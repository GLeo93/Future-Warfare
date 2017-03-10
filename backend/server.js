//dependencies
var express = require('express');           
var mongoose = require('mongoose');         
var bodyParser = require('body-parser')

//connect to mongoDB:	
mongoose.connect('put here your standard MongoDB URI');

//express:
var app = express();
app.use(bodyParser.urlencoded({
  extended: true
}));
app.use(bodyParser.json());

//routes:
app.use('/api', require('./routes/api'));

//start server:
app.listen(8080);
console.log('Server is running on port 8080');
