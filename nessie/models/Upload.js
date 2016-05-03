var mongoose = require('mongoose');

var uploadSchema = new mongoose.Schema({
  user: { type: mongoose.Schema.Types.ObjectId, ref: 'User', index: true },
  name: String,
  status: String,
  url: String
});

module.exports = mongoose.model('Upload', uploadSchema);
