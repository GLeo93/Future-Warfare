//dependencies:
var restful = require('node-restful');
var mongoose = restful.mongoose;

var statusSchema = new mongoose.Schema({
	_id: String,			//as nameGame: name of the game in which it appears
	nextCreator: String,
	latitude: String,
	longitude: String
});

//return models
module.exports = restful.model('supply', statusSchema);