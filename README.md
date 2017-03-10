

#Future Warfare

<p align="center"><img src="http://modernwarfareapp.altervista.org/images/logoreadme.png" width="600" heigth="600"/></p>

The idea of this project is the realization of the classic Laser Game with the support and integration of Android application.
Future Warfare is a game like paintball or airsoft without pain due to his laser nature to simulate the tagging / shooting of other players or targets.

The concept is born with the aim to involve people in this kind of game without the need to go in an apposite Laser game center. With the requirements we’ll see later, every one can finally gets enjoy playing in every moment everywhere.

Thanks to HC-06 module bluetooth, we have the possibilty to always check our position in the map, steadily control our munitions and life, without forgetting enemies in the match. Moreover, the game provides friendly and deathmatch modality: in this way, fun/enjoy is ensured! 



</br><p align="center"><b><a href="http://modernwarfareapp.altervista.org">Visit our Web Site</a></b></p></br>
</br>
You can find the presentation of the project <a href="http://www.slideshare.net/AndreaProsseda/future-warfare-64040999">here</a>

Demo <a href="https://vimeo.com/182035166">here</a>

#Why the new branch?
At the beginning the database of FutureWarfare was hosted in Altervista: Altervista is an italian web platform that provides the possibility to create a web site with PHP, database SQL and FTP access. This implied the use of PHP, but PHP for application realtime and too much database calls is not very efficient.

Trying to find a valid alternative, i focused my attention on Node.js, an event-driven platform for JavaScript V8.

According to <a href="http://blog.soulserv.net/from-php-to-nodejs-part-ii-performances/">these</a> benchmarks, the median result give Javascript roughly 10 times faster than PHP.

Usually benchmarks doesn't tell us the truth, but is well known and confirmed that Javascript/V8 is faster than PHP. It's hard to tell how faster Javascript/V8 is for real world application, but expect +20% to +50% faster than the other one. 

Furthermore Node.js must be chosen with a great number of concurrency requests, so it suits FutureWarfare.

Node.js works perfectly with MongoDB, a NoSQL database. Is well known that performances are increased with non relational DB, so the choice is practically forced: Node.js + MongoDB.

Another important issue, is the LaserGun. With the first version of FutureWarfare it was possible to use only the "Ardunino LaserGun" developed by us. This because shots and deads was managed by Arduino. But with this new version, the bluetooth connection is completely redeveloped: when the gun shots or is hit, it sends a message bluetooth to the app that manages everything. In this way is possible to use every kind of Bluetooth Guns in commerce with a little change of variables in the bluetooth thread.

#Fix and new Features
- The app is completely modified to interact it with the new database and server.
- Threads have been replaced with AsyncTasks. The first one is not simple to use in the correct way, furthermore is not possible to modify the user interface from it. The second one, instead, enables proper and easy use of the UI thread.
- Removed the classic and boring Register/Login activity, giving space to the Facebook Login.
- Now is possible to share on Facebook a post with the details of the game we have created. Our friends can join in our game.
- More controls and checking in background.
- MapsActivity now is more fast. The same for the "i" button [deleted the request of details everytime the button was clicked]
- Now at the end of the game, all data of the game is deleted.
- More improvements of the MapsActivity.
- Add a filter to the JoinGame List: now are shown only game not started.
- Bluetooth connection is completely redeveloped. 
- Add general fixes and improvements.


# Used Tecnologies

<b>Android Side:</b>

- Android Studio

- Google Maps APIs: https://developers.google.com/maps/documentation/android-api

- C9: a cloud IDE: https://c9.io/ 

- Bluetooth Connection [Module HC-06]: http://www.amazon.it/dp/B0113MUGW0

<b>Arduino Side:</b>

- Arduino IDE

- Fritzing: http://fritzing.org/home

- IRremote library: https://github.com/z3t0/Arduino-IRremote

# Architecture 
The architecture of FutureWarfare expects:
- Data Component: it uses MongoDB as NoSQL database
- Server Component: realized using Node.js
- Client Component: as we already discussed, the client side is developed in Android and the smartphone is able to communicate with Arduino thanks to HC-06 module</br>

<p align="center"><img src="http://modernwarfareapp.altervista.org/images/NewArchitecture.png" width="500" heigth="500"/></p>

#Database & Server: MongoDB & Node.js
MongoDB is an increasingly popular document-based, scalable, reliable and high-performance NoSQL database, organized in collections: documents (or simple rows in relational databases) are grouped in collections (tables in relational databases), so collections are sets of documents.

The data schema of FutureWarfare is very simple, since it is composed by 3 collections:
- Games
- PlayersInGame
- Supplies
<p align="center"><img src="http://modernwarfareapp.altervista.org/images/Collections.png" width="600" heigth="600"/></p>

Node.js is a JavaScript runtime built on Chrome's V8 JavaScript engine. Node.js uses an event-driven, non-blocking I/O model that makes it lightweight and efficient, perfect to real-time applications; so it is very suitable to develop scalable and performance applications.
Node.js natively manages a HTTP server library, in this way is possible to run a web server without the use of external software.
</br>

The Future Warfare server is able to interact with the database and with the client: 

1) To comunicate with the database, collections (discussed before) have been modelized thanks to "mongoose", a MongoDB object modeling tool designed to work in an asynchronous environment.
Directly from the official site "Mongoose provides a straight-forward, schema-based solution to model your application data. It includes built-in type casting, validation, query building, business logic hooks and more, out of the box"
Its role is to communicate with the database to obtain documents needed to the client.

2) Thanks to express is possible to comunicate with the client, using RESTful APIs.
Express is a simple but powerful framework that allows to create API REST. 

FutureWarfare implements the following APIs, Format will be JSON, with CRUD functionality: 
Create (Post), Read (Get) Update (Put) Delete (delete).
</br>
<p align="center"><img src="http://modernwarfareapp.altervista.org/images/ApiRest.png" width="500" heigth="500"/> </p></br>
here others custom routes:
<p align="center"><img src="http://modernwarfareapp.altervista.org/images/ApiRestCustom.png" width="500" heigth="500"/></p>

<b>Arduino Side:</b>
</br>
What we need:</br>
</br>
<b>Environment Creation</b></br>
- 1 Arduino Uno</br>
- 1 BreadBoard</br>
- 2 Colored LEDs</br>
- 2 Resistors for LEDs 220 Ohm</br>
- 1 Bluetooth Module HC-06</br>
- 1 Button</br>
- 1 Resistors for Button 10 KOhm</br>
</br>
<b>Core Gun Parts</b></br>
- Receiver: The receiver is a standard IR or Laser receiver module. We use a TSOP38238. It has 3 pins and it use a 220 Ohm Resistor. So the gun knows when it has been shot. The output pin of the receiver drops to a low voltage when a signal is received.</br>
- Transmitter: This is the most expensive part of the project. We need to use a diode laser addressed into a specific lens due to increase the radius of the pointlight. In this way is easier to hit the enemies receiver on their guns. </br>
<b>N.B.</b> If you have some old toy gun as Commodore64 or PS1 guns, the easiest way is to disassemble it and, instead of laser, you have to use a infrared LED because you already have the structure where integrate it (The guns abovementioned are already improved for this kind of infrared trasmission thanks to optical structure that hosts the lens).</br>
<p align="center"><img src="http://modernwarfareapp.altervista.org/images/Lens.png" width="500" heigth="500"/></p>
</br>If you want and if you have it, you can fill the whole Arduino structure in the gun (or if you prefer just receiver and trasmitter) in such a way to use apposite lens of the abovementioned gun. 
</br></br></br>
<b> OSS. </b></br>
The environment creation abovementioned is perfectly suitable for the both solution presented.
</br></br></br>

You can find a demo of Arduino <a href="https://vimeo.com/182035170">here</a> </br> </br>

1) Configure Arduino according to the imagine below </br> </br>

<img src="http://modernwarfareapp.altervista.org/images/Fritzing2.png" width="350" heigth="350"/>	</br></br>

2) Import Arduino code that you can find in the folder Arduino Code -> Future Warfare -> Future Warfare.ino</br></br>

3) Download and Install in your Arduino IDE the library "IRremote" that you can find in the folder Arduino Code -> libraries</br></br>

4) Load code in your Arduino</br></br>

Lighting and sounding scenes advise you of the correctness operation.</br>
After registration, login and creation of the match with the modality set, you can finally begin to game.
The receiver is steadily monitoring for an incoming signal: when an enemy will hit it light and sound will notificate it.</br>
You can continue to play just if your life is greater than three (in deathMatch modality). </br>
In friendly game you can continue to play until the timer set in Android app is over.</br></br>


<b> Android Side:</b></br></br>

1) Install .apk file of our application called Future Warfare on your Android Smartphone</br></br>

2) Turn on GPS and Bluetooth</br></br>

3) Run the App "Future Warfare"</br></br>

4) Pair the Smartphone with Bluetooth Module HC-06 </br></br>

Arm band is recommended </br>
<p align="center"><img src="http://www.photogearetc.com/imglib/images/arkon/XL-ARMBAND/ARK029SM-ARMBAND%20~%20ARKON%20armband%20on%20model%20touch%20screen.jpg" width="500" heigth="500"/></p>


For more information of Android Side you can check the web site, presentation or demo link.</br></br>

Enjoy ;) </br></br>


<b> Developer </b> 
<p align="center">
<img src="http://modernwarfareapp.altervista.org/images/Andrea2.png" width="100" heigth="100"/>  
</br>
<b>Andrea Prosseda</b>
</p>
LinkedIn Page: https://www.linkedin.com/in/andrea-prosseda-2b8651116?trk=hp-identity-name

Email: andreaprosseda@gmail.com

</br></br>
<b> University of Rome "La Sapienza" </b> 

Pervasive Systems page: http://ichatz.me/index.php/Site/PervasiveSystems2016

Students at La Sapienza - University of Rome http://www.uniroma1.it

Master of Science in Engineering in Computer Science http://cclii.dis.uniroma1.it/?q=en/msecs

Department of DIAG http://www.diag.uniroma1.it# Future-Warfare
