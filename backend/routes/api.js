//dependencies:
var express = require('express');
var router = express.Router();

//get models:
var game = require('../models/game');
var playersInGame = require('../models/playersInGame');
var supply = require('../models/supply');

//routes
game.methods(['get', 'post', 'put', 'delete']);
game.register(router, '/game');

playersInGame.methods(['get', 'post', 'put', 'delete']);
playersInGame.register(router, '/playersInGame');

supply.methods(['get', 'post', 'put', 'delete']);
supply.register(router, '/supply');



//custom routes

router.get('/getJoinGames/:username', function(req, res, next) {
  game.find({ 'started': '0' }).where('creator').ne(req.params.username).sort( { _id: +1 } ).exec(function(err, players) {
    if (err) return next(err);
    res.json(players);
  });
});


router.get('/playersInGame/:nameGame', function(req, res, next) {
  playersInGame.find({ 'nameGame': req.params.nameGame }, function (err, players) {
    if (err) return next(err);
    res.json(players);
  });
});


router.post('/supply/:nameGame', function(req, res, next) {
  playersInGame.find({ 'nameGame': req.body.nameGame }).count().exec(function (err, cont) {
    if (err) return next(err);
    var random = Math.floor(Math.random() * cont);
    playersInGame.findOne({ 'nameGame': req.body.nameGame }).skip(random).exec(function(err, nextPlayer) {
      if (err) return next(err);
      supply.create({'_id': req.params.nameGame, 'nextCreator': nextPlayer, 'latitude': req.body.latitude, 'longitude': req.body.longitude}, function(err, todo){
        if(err) return next(err);
        res.json({ message: 'supply created!' });
      });
    });
  });
});


router.put('/nextCreator/:nameGame', function(req, res, next) {
  playersInGame.find({ 'nameGame': req.body.nameGame }).count().exec(function (err, cont) {
    if (err) return next(err);
    var random = Math.floor(Math.random() * cont);
    playersInGame.findOne({ 'nameGame': req.body.nameGame }).skip(random).exec(function(err, nextPlayer) {
      if (err) return next(err);
      supply.findOne({ '_id': req.params.nameGame }).exec(function(err, supp) {
        if (err) return next(err);
        supp.nextCreator = nextPlayer;
        supp.save(function(err) {
                if (err) return next(err);
                res.json({ message: 'nextCreator updated!' });
        });
      });
    });
  });
});


router.get('/winners/:nameGame', function(req, res, next) {
  playersInGame.find({ 'nameGame': req.params.nameGame }).where('lives').ne('0').sort( { lives: -1 } ).exec(function(err, players) {
    if (err) return next(err);
    res.json(players);
  });
});


router.delete('/gameandsupply/:username',function(req, res, next) {
  game.remove({ 'creator': req.body.username }, function(err) {
    if (err) return next(err);
    supply.remove({ 'nextCreator': req.params.username }, function(err) {
      if (err) return next(err);
      playersInGame.remove({ '_id': req.params.username }, function(err) {
        if (err) return next(err);
        res.json({ message: 'Successfully deleted' });
      });
    });
  });
});




//return router:
module.exports = router;