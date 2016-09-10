'use strict';

module.exports = function *processData(value) {
  if (typeof value != 'object') {
  	yield value;
  	return;
  }

  for (let key of Object.keys(value)){
    yield key;
    yield *processData(value[key]);
  }
}