var express = require('express');
var router = express.Router();
var User = require('../models/User');
var Upload = require('../models/Upload');
var aws = require('aws-sdk');


/* GET index page. */
router.get('/', function(req, res, next) {
  return res.redirect('/user/dashboard');
});

/* GET dashboard page. */
router.get('/dashboard', function(req, res, next) {
  return res.render('dashboard', { title: 'Nessie' });
});

router.get('/sign-s3', function(req, res, next) {
  var name = req.query.name;
  var type = req.query.type;
  var bucketName = 'teamnessie';

  var s3 = new aws.S3();
  var s3Options = {
    Bucket: bucketName,
    Key: req.user.id + '/' + name,
    Expires: 60,
    ContentType: type,
    // TODO do we want this to be public read
    ACL: 'public-read'
  };
  s3.getSignedUrl('putObject', s3Options, function(err, data) {
    if (err){
      return res.sendStatus(500);
    } else {
      var upload = new Upload({
        user: req.user.id,
        status: 'Uploading',
        name: name
      });
      var return_data = {
        signed_request: data,
        url: 'https://'+ bucketName +'.s3.amazonaws.com/' + req.user.id + '/' + name,
        upload_id: upload.id
      };
      upload.url = return_data.url;
      upload.save(function(err) {
        if (err) {
          res.sendStatus(500);
        }
      });
      return res.json(return_data);
    }
  });
});

router.post('/update-upload', function(req, res, next) {
  // TODO: validate these params
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

router.get('/get-uploads', function(req, res, next) {
  Upload.find({ user: req.user.id }, function(err, uploads) {
    if (err) {
      return res.sendStatus(500);
    }
    return res.json(uploads);
  })
});


module.exports = router;
