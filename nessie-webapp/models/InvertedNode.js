var mongoose = require('mongoose');

var invertedNodeSchema = new mongoose.Schema({
	term: {type: String, ref: "Key", index: true},
	nodeId: String
});

module.exports = mongoose.model('InvertedNode', invertedNodeSchema);
