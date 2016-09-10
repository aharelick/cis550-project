'use strict';

var xml2js         = require('xml2js');
var Converter      = require('csvtojson').Converter;
var Graph          = require('./graph');
var Node           = require('./node');
var ObjectIterator = require('./objectIterator');

module.exports.parseForm = function(req) {
  var form = new Formidable.IncomingForm();
  return new Promise(function(resolve, reject) {
    form.parse(req, function(err, fields, files) {
      if (err) {
        reject('Couldn\'t parse form');
      }
      else {
        resolve({
          fields: fields,
          files: files
        });
      }
    });
  });
};

module.exports.readFilePromise = function(form) {
  return new Promise(function(resolve, reject) {
    fs.readFile(form.files.path, 'utf8', function(err, data) {
      if (err) {
        reject('Couldn\' read file');
      }
      else {
        resolve({
          form: form,
          data: data
        });
      }
    });
  });
};

module.exports.parseFileContents = function(vals) {
  switch (vals.form.fields.type) {
    case 'application/json':
      return Promise.resolve({
        form: vals.form,
        data: JSON.parse(vals.data)
      });

    case 'text/xml':
      var parser = new xml2js.Parser({'attrKey': 'attrs'});
      return new Promise(function(resolve, reject) {
        parser.parseString(vals.data, function(err, result) {
          if (err) {
            return reject('Coudln\'t parse xml file');
          }
          else {
            resolve({
            	form: vals.form,
            	data: result
            });
          }
        });
      });

    case 'text/csv':
      var converter = new Converter({});
      return new Promise(function(resolve, reject) {
        converter.fromString(vals.data, function(err, result) {
          if (err) {
            return reject('Couldn\'t parse csv file');
          }
          else {
            resolve({
            	form: vals.form,
            	data: result
            });
          }
        });
      });
  }
};

module.exports.createFileUpload = function(vals) {
	Upload.find({name: vals.form.fields.name}, function(err, uploads) {
		if (uploads.length > 0) {
			return Promise.reject('File with this name alrady exists');
		} else {
			return Promise.resolve(uploads);
		}
	})
	.then(function(uploads) {
        var upload = new Upload({
          user: req.user.id,
          status: 'Upload Complete',
          name: fields.name,
          type: fields.type
        });
        upload.save(function(err, writeResult) {
          if (err) {
            return Promise.reject("Couldn't create upload");
          } else {
            return Promise.resolve(writeResult._id);
          }
        });
	});
};

module.exports.createGraph = function(vals) {
	var rootNode = new Node('root');
  createGraphRecursive(rootNode, vals.data);
  graphTraversal(rootNode);

  function createGraphRecursive(node, data) {
    // We've reached the end of the recursion
    if (typeof data != 'object') {
      node.neighbors.push(new Node(data));
    }

    // Create all of the children nodes
    for (let key of Object.keys(data)) {
      var childNode = new Node(key);
      node.neighbors.push(childNode);
      createGraphRecursive(childNode, data[key]);
    }
  }

  function graphTraversal(node) {
    console.log('value: ' + node.value);
    for (let child of node.neighbors) {
      graphTraversal(child, level + 1);
    }
  }
}
