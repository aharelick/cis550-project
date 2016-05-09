var mongoose = require('mongoose');

var nodeSchema = new mongoose.Schema({
	key: {type: String, index: true},
	docId: String,
	parent: String,
	neighbors: []
});

module.exports = mongoose.model('Node', nodeSchema);
