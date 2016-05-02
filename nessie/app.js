var express = require('express');
var path = require('path');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var passport = require('passport');
var session = require('express-session');
var mongoose = require('mongoose');
var MongoStore = require('connect-mongo')(session);
var flash = require('connect-flash');
var expressValidator = require('express-validator');
require('./config/pass')(passport);

var routes = require('./routes/index');

var app = express();

/**
 * Load environment variables.
 */

var config;
if (app.get('env') === 'development') {
  config = require('./config/config');
} else {
  config = {
    MONGODB_URI: process.env.MONGODB_URI,
    SESSION_SECRET: process.env.SESSION_SECRET
  }
}

/**
 * Connect to MongoDB.
 */

mongoose.connect(config.MONGODB_URI);
mongoose.connection.on('error', function() {
  console.error('MongoDB Connection Error. Please make sure that MongoDB is running.');
});


// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(expressValidator());
app.use(cookieParser());
app.use(session({
  resave: false,
  saveUninitialized: false,
  store: new MongoStore({
    url: config.MONGODB_URI,
    autoReconnect: true
  }),
  secret: config.SESSION_SECRET,
}));
app.use(passport.initialize());
app.use(passport.session());
app.use(flash());

app.use(function(req, res, next) {
  res.locals.messages = {
    success: req.flash('success'),
    errors: req.flash('errors'),
    form_errors: req.flash('form-errors'),
    form_errors_bool: req.flash('form-errors').length !== 0
  };
  next();
});

app.use(express.static(path.join(__dirname, 'public')));

app.use('/', routes);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
  app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
      message: err.message,
      status: err.status,
      error: err
    });
  });
}

// production error handler
// no stacktraces leaked to user
app.use(function(err, req, res, next) {
  res.status(err.status || 500);
  res.render('error', {
    message: err.message,
    status: err.status,
    error: {}
  });
});


module.exports = app;
