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

/* GET signup page. */
router.get('/signup', function(req, res, next) {
  return res.render('signup', { title: 'Signup' });
});

/* POST signup. */
router.post('/signup', function(req, res, next) {
  req.assert('email', 'Email is not valid').isEmail();
  req.assert('password', 'Password must be at least 6 characters long').len(6);
  req.assert('confirm', 'Passwords do not match').equals(req.body.password);

  var errors = req.validationErrors();

  if (errors) {
    req.flash('form-errors', errors);
    return res.redirect('/signup');
  }

  var user = new User({
    email: req.body.email,
    password: req.body.password
  });

  User.findOne({ email: req.body.email.toLowerCase() }, function(err, existingUser) {
    if (existingUser) {
      req.flash('errors', 'Account with that email address already exists.');
      return res.redirect('/signup');
    }
    user.save(function(err) {
      if (err) return next(err);
      req.login(user, function(err) {
        if (err) return next(err);
        return res.redirect('/user/dashboard');
      });
    });
  });
});

router.get('/logout', function(req, res) {
  req.logout();
  return res.redirect('/login');
});

module.exports = router;
