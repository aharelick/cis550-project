'use strict';

var assert = require('chai').assert;
var flow = require('../routes/flow.js');
var fs = require('fs');
var process = require('process');
var path = require('path');
var crypto = require('crypto');

describe('Flow: ', function() {
  describe('#readFilePromise', function() {
    var readStream = fs.createReadStream(path.resolve(__dirname, '../data/philly_educator_information.csv'));
    readStream.setEncoding('utf8');
    readStream.on('data', function (chunk) {
      console.log(chunk);
      console.log("DATA");
    })
      .on('end', function () {
        console.log('end');
      });
  });

  describe('#parseForm', function() {

  });

  describe('#parseFileContents', function() {
  	var vals;
  	// Create mock object to use as input
  	beforeEach(function() {
  		vals = {form: {fields: {type: ''}}, data: ''};
  	});

  	it('parse a valid json file', function(done) {
  		vals.form.fields.type = 'application/json';
  		vals.data = fs.readFileSync(path.resolve(__dirname, '../data/sample.json'), 'utf-8');

  		flow.parseFileContents(vals)
  		.then(function(result) {
  			assert.isDefined(result.data.glossary);
  			done();
  		})
  		.catch(function(err) {
  			// Should never get to the catch so fail test if we do
  			assert.isTrue(false);
  		})
  	});

  	it('parse a valid xml file', function(done) {
  		vals.form.fields.type = 'text/xml';
  		vals.data = fs.readFileSync(path.resolve(__dirname, '../data/sample.xml'), 'utf-8');

  		flow.parseFileContents(vals)
  		.then(function(result) {
  			assert.isDefined(result.data.catalog);
  			done();
  		})
  		.catch(function(err) {
  			// Should never get to the catch so fail test if we do
  			assert.isTrue(false);
  		});
  	});

  	it('parse a valid csv file', function(done) {
  		vals.form.fields.type = 'text/csv';
  		vals.data = fs.readFileSync(path.resolve(__dirname, '../data/sample.csv'), 'utf-8');

  		flow.parseFileContents(vals)
  		.then(function(result) {
			assert.isDefined(result.data[0].term);
			done();
  		})
  		.catch(function() {
  			// Should never get to the catch so fail test if we do
  			assert.isTrue(false);
  		})
  	})
  });

  describe('#createFileUpload', function() {
  });

  describe('#createGraph', function() {
  	var vals;
  	beforeEach(function() {
		  vals = {form: {fields: {type: ''}}, data: ''};
  	});

  	it('iterating through graph', function() {
  		vals.form.fields.type = 'application/json';
  		vals.data = fs.readFileSync(path.resolve(__dirname, '../data/sample.json'), 'utf-8');

  		flow.parseFileContents(vals)
  		.then(function(result) {
  			flow.createGraph(result);
  		})
  		.catch(function() {
  			assert.isTrue(false);
  		})
  	});
  });
});
