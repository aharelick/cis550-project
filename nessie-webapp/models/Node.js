var mongoose = require('mongoose');

var nodeSchema = new mongoose.Schema({
	key: {type: String, ref: "Key", index: true},
	docId: String,
	parent: String,
	neighbors: []
});

module.exports = mongoose.model('Node', nodeSchema);
