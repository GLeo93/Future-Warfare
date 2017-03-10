//dependencies:
var restful = require('node-restful');
var mongoose = restful.mongoose;

var statusSchema = new mongoose.Schema({
	//player can appear in this table 0/1 time (he can join in a game at time)
	_id: String,		//username of the player
	nameGame: String,
	latitude: String,
	longitude: String,
	lives: String
});

//return models
module.exports = restful.model('playersInGame', statusSchema);