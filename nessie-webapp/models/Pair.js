var mongoose = require('mongoose');

var pairSchema = new mongoose.Schema({
	key: {type: String, ref: "Key", index: true},
	value: {type: String, ref: "Value", index:true},
	docId: String,
	path: String,
	neighbors: [],
});

module.exports = mongoose.model('Pair', pairSchema);
