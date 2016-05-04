var mongoose = require('mongoose');

var uploadSchema = new mongoose.Schema({
  user: { type: mongoose.Schema.Types.ObjectId, ref: 'User', index: true },
  name: String,
  type: String,
  status: String,
  url: String,
  public: { type: Boolean, default: false }
});

module.exports = mongoose.model('Upload', uploadSchema);
