var express = require('express');
var passport = require('passport');
var router = express.Router();
var User = require('../models/User');


/* GET home page. */
router.get('/', function(req, res, next) {
  if (req.isAuthenticated()) {
    return res.redirect('/user/dashboard');
  } else {
    return res.redirect('/login');
  }
});

/* GET login page. */
router.get('/login', function(req, res, next) {
  return res.render('login', { title: 'Login' });
});

/* POST login. */
router.post('/login', function(req, res, next) {
  passport.authenticate('local', function(err, user, info) {
    if (err) { return next(err); }
    if (!user) {
      req.flash('errors', 'Incorrect email or password.');
      return res.redirect('/login');
    }
    req.login(user, function(err) {
      if (err) { return next(err); }
      return res.redirect('/user/dashboard');
    });
  })(req, res, next);
});

module.exports = router;
