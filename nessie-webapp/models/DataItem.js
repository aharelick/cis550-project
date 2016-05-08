var mongoose = require('mongoose');

var dataItemSchema = new mongoose.Schema({
	fileName: {type: String, index:true},
	data: mongoose.Schema.Types.Mixed,
	neighbors: []
});

module.exports = mongoose.model('DataItem', dataItemSchema);
