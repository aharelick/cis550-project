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
<<<<<<< Updated upstream
var xml2js     = require('xml2js');
var Converter  = require("csvtojson").Converter;
var search     = require('./search.js');
=======
var xml2js = require('xml2js');
var Converter = require("csvtojson").Converter;
>>>>>>> Stashed changes

/* GET index page. */
router.get('/', function(req, res, next) {
  return res.redirect('/user/dashboard');
});

/* GET dashboard page. */
router.get('/dashboard', function(req, res, next) {
  return res.render('dashboard', { title: 'Nessie' });
});

<<<<<<< Updated upstream
=======
/* GET search results page*/
router.get('/searchResults', function(req, res, next) {
  return res.render('searchResults', {test : "hello!"});
});
>>>>>>> Stashed changes

router.get('/search', function(req, res, next) {
  return res.render('search', {title: 'Search'});
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

router.post('/search', function(req, res, next) {
  terms = req.body.searchTerms.split(' ');
  terms.forEach(function(term, index, array) {
    Node.find({key: term}, function(err, nodes) {
      if (err) {
        console.log(err);
      } else {
        var path = [];
        if (nodes.length > 0) {
          recursiveUpToRoot(nodes[0], path, res);
        } else {
          return res.json({message: "No matching nodes found"});
        }
      }
    });
  });
})

var recursiveUpToRoot = function(node, path, res) {
  path.push(node);
  if (node.parent == null) {
    return res.json(path);
  }
  Node.findOne({_id: node.parent}, function(err, parent) {
    recursiveUpToRoot(parent, path, res);
  });
}

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
    Upload.find({name: fields.name}, function(err, uploads) {
      if (uploads.length > 0) {
        console.log(util.inspect(uploads, false, null));
        return res.json({message: 'Item with given filename is already in DB'})
      } else {

        var upload = new Upload({
          user: req.user.id,
          status: 'Created Upload',
          name: fields.name,
          type: fields.type
        });
        upload.save(function(err, writeResult) {
          if (err) {
            console.log(err);
            return res.sendStatus(500);
          } else {

            var nodes = [];
            var invertedNodes = [];
            var parentNode = addNode(fields.name, null, writeResult._id, nodes, invertedNodes);
            createNodes(contents, parentNode, writeResult._id, nodes, invertedNodes);
            Node.insertMany(nodes, function(err, writeResult) {
              if (err) {
                return res.sendStatus(500);
              }
              InvertedNode.insertMany(invertedNodes, function(err, writeResult) {
                if (err) {
                  console.log(err);
                  return res.sendStatus(500);
                } else {
                  console.log(writeResult);
                  return res.json({success:true})
                }
              })
            });
          }
        });
      }
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
      term: term,
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

router.get('/search', function(req, res, next) {
  return res.render('search', { title: 'Search' });
});



module.exports = router;
