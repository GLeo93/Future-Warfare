//dependencies:
var restful = require('node-restful');
var mongoose = restful.mongoose;

var statusSchema = new mongoose.Schema({
	_id: String,			//name of the game 
	kindOfGame: String,
	location: String,
    players: String,
	startTime: String,
	startDate: String,
	duration: String,
	creator: String,
	started: String
});

//return models
module.exports = restful.model('game', statusSchema);