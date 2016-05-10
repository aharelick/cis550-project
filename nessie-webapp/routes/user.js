var fs = require('fs');
var util = require('util');
var express = require('express');
var router = express.Router();
var User = require('../models/User');
var Upload = require('../models/Upload');
var Node = require('../models/Node');
var InvertedNode = require('../models/InvertedNode');
var aws = require('aws-sdk');
var formidable = require('formidable');
var xml2js     = require('xml2js');
var Converter  = require("csvtojson").Converter;
var async  = require('async');
var kue = require('kue');
var search = require('./search.js');

var jobs = kue.createQueue({
  redis: process.env.REDIS_URL || 'redis://localhost:6379'
});


/* GET index page. */
router.get('/', function(req, res, next) {
  return res.redirect('/user/dashboard');
});

/* GET dashboard page. */
router.get('/dashboard', function(req, res, next) {
  return res.render('dashboard', { title: 'Nessie' });
});


router.get('/search', function(req, res, next) {
    return res.render('search', { title: 'Search' });
});
router.post('/search', function(req, res, next) {
  var query = req.body.searchTerms;
  search(query, function(paths) {
    res.json(paths);
  });
})


router.get('/sign-s3', function(req, res, next) {
  var uploadId = req.query.upload_id;
  var bucketName = 'teamnessie';

  Upload.findById(uploadId, function(err, upload) {
    if (err) {
      return res.sendStatus(500);
    }
    var s3 = new aws.S3();
    var s3Options = {
      Bucket: bucketName,
      Key: req.user.id + '/' + upload.name,
      ContentType: upload.type,
      // TODO: do we want this to be public read
      ACL: 'public-read'
    };
    s3.getSignedUrl('putObject', s3Options, function(err, data) {
      if (err){
        return res.sendStatus(500);
      } else {
        var return_data = {
          signed_request: data,
          url: 'https://'+ bucketName +'.s3.amazonaws.com/' + req.user.id + '/' + upload.name
        };
        upload.url = return_data.url;
        upload.status = 'Created Signed URL';
        upload.save(function(err) {
          if (err) {
            res.sendStatus(500);
          }
        });
        return res.json(return_data);
      }
    });
  });
});

router.post('/create-upload', function(req, res, next) {

  // Create the tree and store it in Mongo
  var form = new formidable.IncomingForm();
  form.parse(req, function(err, fields, files) {
    var contents;

    // Parse the document and convert it to a json object
    if (fields.type === 'application/json') {
      contents = JSON.parse(fs.readFileSync(files.file.path));
    }
    else if (fields.type === 'text/xml') {
      var parser = new xml2js.Parser({'attrkey': 'attrs'});
      parser.parseString(fs.readFileSync(files.file.path, 'utf8'), function(err, result) {
        contents = result;
      })
    }
    else if (fields.type === 'text/csv') {
      var converter = new Converter({});
      converter.fromString(fs.readFileSync(files.file.path, 'utf8'), function(err,result){
        contents = result;
      });
    }

    // Look for the document in the database, if it isn't already present then add it
    async.waterfall([
      // Check if the upload already exists in the db
      function(callback) {
        Upload.find({name: fields.name}, function(err, uploads) {
          if (uploads.length > 0) {
            return callback('File already exists.');
          } else {
            callback(null, uploads);
          }
        });
      },
      // Save the Upload
      function(uploads, callback) {
        var upload = new Upload({
          user: req.user.id,
          status: 'Upload Complete',
          name: fields.name,
          type: fields.type
        });
        upload.save(function(err, writeResult) {
          if (err) {
            return callback(err);
          }
          else {
            callback(null, writeResult._id);
          }
        });
      },
      // Create the nodes
      function(docId, callback) {
        var nodes = [];
        var invertedNodes = [];
        var parentNode = addNode(fields.name, null, docId, nodes, invertedNodes);
        createNodes(contents, parentNode, docId, nodes, invertedNodes);

        var job = jobs.create('upload', {
          nodes: nodes,
          invertedNodes: invertedNodes
        });

        job.on('complete', function(){
          console.log("Job complete");
        }).on('failed', function(){
          console.log("Job failed");
        }).on('progress', function(progress){
          console.log('job #' + job.id + ' ' + progress + '% complete');
        });

        job.save();

        callback(null, nodes, invertedNodes);
      }/*,
      // For each node start the next pipeline
      function(nodes, invertedNodes, callback) {
        async.eachSeries(nodes, function(node, callbackTwo) {
          async.waterfall([
            // Get all of the inverted neighbors of this node
            async.apply(function(node, callbackThree) {
              console.log('Getting inverted neighbors');
              terms = node.key.split(' ');
              InvertedNode.find({term: {$in: terms}}, function(err, invertedNeighbors) {
                if (err) {
                  return callbackThree(err);
                } else {
                  callbackThree(null, node, invertedNeighbors);
                }
              });
            }, node),
            // Using the inverted neighbor ids, get the neighbor nodes
            function(node, invertedNeighbors, callbackThree) {
              console.log('Getting neighbors');
              neighborIds = [];
              invertedNeighbors.forEach(function(invertedNeighbor, index) {
                neighborIds.push(invertedNeighbor.nodeId);
              });

              Node.find({_id: {$in: neighborIds}}, function(err, neighbors) {
                if (err) {
                  return callbackThree(err);
                } else {
                  callbackThree(null, node, neighbors)
                }
              });
            },
            // Add neighbors to the adjacency lists
            function(node, neighbors, callbackThree) {
              console.log('Adding neightbors to the adjacency list');
              neighbors.forEach(function(neighbor, index) {
                if (!(neighbor._id in node.neighbors) && !(node._id in neighbor.neighbors)) {
                  node.neighbors.push(neighbor._id);
                  neighbor.neighbors.push(node._id);
                  neighbor.save();
                }
              })
              callbackThree(null);
            }], function(err, result) {
              Node.insertMany(nodes);
              InvertedNode.insertMany(invertedNodes);
              callbackTwo(err);
            });
        }, function(err) {
          callback(err);
        });
      }*/
      ], function(err, result) {
        if (err) {
          return res.json({message: err});
        }
        return res.json({message: 'Success'});
      });
  });
});


// Iterate through the dataItem and add all entries to an inverted index
var createNodes = function(value, parent, fileId, nodes, invertedNodes) {
  if (Array.isArray(value)) {
    value.forEach(function(currentVal, index) {
      createNodes(currentVal, parent, fileId, nodes, invertedNodes);
    });
  }
  else if (typeof value === 'object' && value != null) {
    Object.keys(value).forEach(function(key, index, array) {
      var node = addNode(key, parent, fileId, nodes, invertedNodes);
      createNodes(value[key], node, fileId, nodes, invertedNodes);
    });
  } else {
    addNode(value, parent, fileId, nodes, invertedNodes);
  }
}

var addNode = function(key, parent, fileId, nodes, invertedNodes) {
  key = '' + key;
  var node = new Node({
    key: key.toLowerCase(),
    docId: fileId,
    neighbors: [],
    parent: parent == null ? null : parent._id
  });

  if (parent != null) {
    node.neighbors.push(parent._id);
    parent.neighbors.push(node._id);
  }

  key.split(' ').forEach(function(term, index) {
    var invertedNode = new InvertedNode({
      term: term.toLowerCase(),
      nodeId: node._id
    });
    invertedNodes.push(invertedNode);
  });

  nodes.push(node);
  return node;
}


router.post('/update-upload-status', function(req, res, next) {
  // TODO: validate these params, make sure user is editing own upload
  var id = req.body.id;
  var status = req.body.status;
  Upload.findOne({ _id: id }, function(err, upload) {
    if (err) {
      return res.sendStatus(500);
    }
    upload.status = status;
    upload.save(function(err) {
      if (err) {
        return res.sendStatus(500);
      }
    });
    return res.sendStatus(200);
  });
});

router.get('/edit-upload/:id', function(req, res, next) {
  // TODO: validate this id and make sure it belongs to the current user
  var id = req.params.id;
  Upload.findById(id, function(err, upload) {
    if (err) {
      return res.redirect('/user/dashboard');
    } else {
      return res.render('edit_upload', { upload: upload, title: 'Edit Upload' });
    }
  });
});

router.post('/edit-upload/:id', function(req, res, next) {
  // TODO: validate id parameter, make sure user is allowed to edit it
  var id = req.params.id;
  Upload.findById(id, function(err, upload) {
    if (err) {
      return res.redirect('/user/dashboard');
    }
    upload.public = (req.body.public !== undefined);
    upload.save(function(err) {
      if (err) {
        return next(err);
      }
      return res.redirect('/user/dashboard');
    });
  });
});

router.get('/get-uploads', function(req, res, next) {
  Upload.find({ user: req.user.id }, function(err, uploads) {
    if (err) {
      return res.sendStatus(500);
    }
    return res.json(uploads);
  });
});



module.exports = router;
