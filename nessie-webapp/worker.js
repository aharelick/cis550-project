var kue = require('kue');
var url = require('url');
//var redis = require('redis');


/**
 * Load environment variables.
 */

config = require('./config/config');

//kue.redis.createClient = function() {
//  var redisUrl = url.parse(config.REDIS_URL);
//  var client = kue.redis.createClient(redisUrl.port, redisUrl.hostname);
//  if (redisUrl.auth) {
//    client.auth(redisUrl.auth.split(":")[1]);
//  }
//  return client;
//};

var jobs = kue.createQueue();

// see https://github.com/learnBoost/kue/ for how to do more than one job at a time
jobs.process('crawl', function(job, done) {
  setTimeout(function(){
    console.log(job.data);
    done();
  }, 15000);
});
