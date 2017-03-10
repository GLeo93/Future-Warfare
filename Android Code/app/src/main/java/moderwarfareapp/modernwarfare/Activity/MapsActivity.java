package moderwarfareapp.modernwarfare.Activity;

import android.Manifest;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import moderwarfareapp.modernwarfare.AsyncTasks.BluetoothTask;
import moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.NumberOfPlayersInGame;
import moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.SupplyTasks.AddSupplyInGame;
import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.DeleteGame;
import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.NumberOfWaitingPlayers;
import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.DeleteUserInGame;
import moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.GetDetailsGame;
import moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.Markers.MapSupportTask;
import moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.Markers.PutCoordinates;
import moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.EndOfGame.GameOverManagement;
import moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.SupplyTasks.PutNewCoordinatesSupply;
import moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.SupplyTasks.PutNextCreatorSupply;
import moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.SupplyTasks.SupplyCreatorTask;
import moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.SupplyTasks.SupplyManager;
import moderwarfareapp.modernwarfare.R;
import moderwarfareapp.modernwarfare.Utility.BluetoothThread;
import moderwarfareapp.modernwarfare.Utility.GlobalValue;
import moderwarfareapp.modernwarfare.Utility.Hex;
import moderwarfareapp.modernwarfare.Utility.PermissionUtils;
import moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.SupplyTasks.WaitingTask;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;      //fields required to manage map in this Activity
    private boolean mPermissionDenied = false;
    private UiSettings mUiSettings;
    private GoogleMap mMap;

    private Marker[] marks;                             //this array contains markers of all players connected in the same game
    private Circle circle;                              //this field will contain the supply area

    private Handler handler = new MyHandler();          //handler required to exchange information with the main activity
    private AsyncTask mapSupportTask;                   //this thread updates positions of players on the map (notifying when insert and delete them)
    private AsyncTask checkNPlayersTask;                //this thread check if there is only 1 player connected in the game, then notify the end of the game
    private AsyncTask supplyManager;                    //this thread periodically checks and updates supply position
    private AsyncTask supplyCreatorTask;                //this thread periodically checks if current player is able to create a new supply
    private BluetoothThread btThread;                   //this thread manages the messages exchange with arduino's bluetooth (asking #shots and #dead)


    //game details
    private String username, nameGame, start, date, location, creator, kindOfGame;
    private int players;
    private long duration;
    String usernameHex;
    String nameGameHex;

    //player details
    private final int totalShot = 100;
    private int shot = 100;                             //remaining shots [current shots]
    private int dead = 0;                               //number of dead in the game
    private int lives = 3;                              //used if kindOfGame is DeathMatch
    private LatLng myPosition;                          //updated with current position of the player, used in inRange() and other methods
    private LatLng old_supply= new LatLng(0,0);         //used in inRange(), to control if we are still (or we was) on the supply area

    private TextView textViewTime;                      //this textview will contain the remaining time of the game

    private boolean firstIteration = true;              //used to insert (only 1 time) a fake supply on DB
    private boolean firstTime = true;                   //used to start (only 1 time) counter in getDataGame method (this method is used many times)
    private boolean deathMatch;                         //used to check if current game is a DeathMatch or not. If is true, we have different rules


    @Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    //take fields from the previous Activity
    Intent intent = getIntent();
    nameGame = intent.getStringExtra("nameGame");
    nameGameHex = Hex.convertStringToHex(nameGame);
    username = intent.getStringExtra("username");
    usernameHex = Hex.convertStringToHex(username);
    kindOfGame = intent.getStringExtra("kindOfGame");

    if(kindOfGame.equals("Death Match"))
        deathMatch = true;
    else
        deathMatch = false;


    String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/game/" + nameGameHex;
    new GetDetailsGame(MapsActivity.this, getApplicationContext(), handler).execute(httpUrl);

    //this textview show remaining time of the game
    textViewTime = (TextView) findViewById(R.id.textViewTime);

    SupportMapFragment mapFragment =
            (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

    //is the "i" button
    fab.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if(deathMatch) {
                Snackbar.make(view, "Game: " + nameGame + "\t\t\t\t" + "Shot: " + shot + "/" + totalShot + "\t\t\t\t" + "Lives: " + lives +
                        "\t\t\t\t" + "Players: " + players + "\t\t\t\t" + "Time: " + duration + " min", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            else{
                Snackbar.make(view, "Game: " + nameGame + "\t\t\t\t" + "Shot: " + shot + "/" + totalShot + "\t\t\t\t" + "Dead: " + dead +
                        "\t\t\t\t" + "Players: " + players + "\t\t\t\t" + "Time: " + duration + " min", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    });

   startAsyncTasks();

    //now we want to check if gps location is enabled and works
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    boolean gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

    if (!gps) { //if gps is off a message is shown
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setMessage("Please turn on your GPS").setNegativeButton("Ok", null).create().show();
    } else {
        //if it is on, user has to allow permission, mandatory to use the map
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setMessage("Please allow permission on GPS").setNegativeButton("Ok", null).create().show();
            return;
        }
        //position of the player is update every 20s or every 50 meters
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000, // 2 seconds interval between updates
                50, // 50 meters between updates
            new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //if the location is changed, take latitude and longitude and send them to server with a JSON request
                    String latitude = String.valueOf(location.getLatitude());
                    String longitude = String.valueOf(location.getLongitude());

                    myPosition = new LatLng(location.getLatitude(),location.getLongitude());
                    GlobalValue.getInstance().setMyPosition(myPosition);

                    if (firstIteration) {
                        if (creator.equals(username)) {
                            String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/supply/" + nameGameHex;
                            new AddSupplyInGame(MapsActivity.this, nameGame).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, httpUrl);
                        }

                        firstIteration = false;

                        String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/supply/" + nameGameHex;

                        supplyCreatorTask = new SupplyCreatorTask(handler, username).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, httpUrl);
                        supplyManager = new SupplyManager(handler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, httpUrl);

                    }
                    //update coordinates on db
                    String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/playersingame/" + usernameHex;
                    //make Get Request
                    new PutCoordinates(MapsActivity.this, username, latitude, longitude).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, httpUrl);
                }

                @Override
                public void onProviderDisabled(String provider) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

            });
        }
    }

    //this handler helps the execution of the thread
    private class MyHandler extends Handler {

        //receives messages by thread
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            JSONArray jsonArray = new JSONArray();

            //if message contains "updateMap", markers of user positions must be shown in the map
            if (bundle.containsKey("DetailGame")){
                String value = bundle.getString("DetailGame");

                try {
                    JSONObject jsonResponse = new JSONObject(value);
                    duration = Long.parseLong(jsonResponse.getString("duration"));
                    location = jsonResponse.getString("location");
                    start = jsonResponse.getString("startTime");
                    date = jsonResponse.getString("startDate");
                    creator = jsonResponse.getString("creator");
                    // data are populated, so a timer can start

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //start the counter!
                if(firstTime){
                    final CounterClass timer = new CounterClass(duration * 60000, 1000); //rimettere lo 0
                    timer.start();
                    firstTime = false;
                }
            }
            else if(bundle.containsKey("updateMap")) {
                String value = bundle.getString("updateMap");
                try {
                    jsonArray = new JSONArray(value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                marks = new Marker[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = null;
                    try {
                        jo = jsonArray.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        if((!jo.isNull("latitude") && !jo.isNull("longitude")) && !jo.getString("_id").equals(usernameHex)) {
                            marks[i] = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(jo.getDouble("latitude"), jo.getDouble("longitude")))
                                    .title(Hex.convertHexToString(jo.getString("_id")))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            //if message contains "clear", the map will be totally clean but if there is a supply this will reloaded
            else if (bundle.containsKey("clear")) {
                if (circle != null) {
                        LatLng pos = circle.getCenter();
                        mMap.clear();      //if the message is "clear" markers must be deleted from the map
                        circle = mMap.addCircle(new CircleOptions()
                                .center(pos)
                                .radius(50)
                                .strokeColor(Color.BLACK)
                                .strokeWidth(3)
                                .fillColor(Color.HSVToColor(80, new float[]{105, 1, 1})));
                } else {
                    mMap.clear();      //if the message is "clear" markers must be deleted from the map
                }
            }

            //check if a player is hit, increasing the #dead in frindly match or decreasing the #lives in Death match showing when you are hit
            else if (bundle.containsKey("hit")) {      //is the manager of number of deads
                dead++;

                if (deathMatch) {
                    lives--;
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setTitle("You Are Dead").setMessage("Remaining lives: " + lives).setPositiveButton("Ok", null).create().show();
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setTitle("You Are Dead").setPositiveButton("Ok", null).create().show();
                }

                if (lives == 0) {       //furthermore check if your lives are 0, then your game ends
                    sendMessageBluetooth("2"); //stop the gun
                    gameOver();
                }
            }

            //updating the #shot, taking them by Arduino
            else if (bundle.containsKey("shot")){      //is the manager of remaining shots
                shot--;
                if(shot<1) {
                    sendMessageBluetooth("2"); //stop the gun
                }
            }

            //asynctask notify to this activity if currentPlayer is the last one player in the game
            else if (bundle.containsKey("players")){
                players = bundle.getInt("players");
                if(players == 1) {
                    if(deathMatch)
                        getWinners();
                }
            }

            //CheckSupplyThread periodically update the supply position
            else if(bundle.containsKey("supply")) {
                double lat = bundle.getDouble("latitude");
                double lon = bundle.getDouble("longitude");
                if(circle!=null)
                    circle.remove();

                circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(lat, lon))
                        .radius(50)
                        .strokeColor(Color.BLACK)
                        .strokeWidth(3)
                        .fillColor(Color.HSVToColor(80, new float[]{105, 1, 1})));

                if(inRange()){
                    if(old_supply.latitude != circle.getCenter().latitude && old_supply.longitude != circle.getCenter().longitude) {
                        old_supply = new LatLng(circle.getCenter().latitude, circle.getCenter().longitude);
                        if(shot<1) {
                            sendMessageBluetooth("1"); //start the gun
                        }
                        shot = totalShot;
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                        builder.setMessage("Shots reloaded").setPositiveButton("Ok",null).create().show();
                        System.out.println("supply taken");
                    }
                }

            }

            //WaintingThread notify to remove the supply after 30 second
            else if(bundle.containsKey("remove")){
                if(circle!=null) {
                    circle.remove();
                    circle = null;
                }

                String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/nextCreator/" + nameGameHex;
                new PutNextCreatorSupply(MapsActivity.this, getApplicationContext(), nameGame).execute(httpUrl);
            }

            //SupplyCreatorThread notify to WaintingThread to start when he insert a supply on the DB
            else if (bundle.containsKey("updateCoordinatesSupply")) {
                String lat = bundle.getString("latitude");
                String lon = bundle.getString("longitude");

                String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/supply/" + nameGameHex;
                new PutNewCoordinatesSupply(MapsActivity.this, nameGame, lat, lon).execute(httpUrl);

                new WaitingTask(handler).execute();
            }
        }
    }

    //check if the player reach the supply area
    public boolean inRange(){
        if(circle==null) {
            return false;
        }
        LatLng center = circle.getCenter();
        double distance = Math.sqrt(Math.pow(myPosition.latitude - center.latitude, 2) + Math.pow(myPosition.longitude - center.longitude, 2));
        if(distance < 0.0011616970345136063) {
            return true;
        }
        return false;
    }

    //when player is dead 3 times, his game ends
    public void gameOver(){
        textViewTime.setText("");
        mapSupportTask.cancel(true);
        checkNPlayersTask.cancel(true);
        btThread.stopThread();

        if(supplyCreatorTask.getStatus() == AsyncTask.Status.RUNNING)
            supplyCreatorTask.cancel(true);       //stop these asynctask if they are started
        if(supplyManager.getStatus() == AsyncTask.Status.RUNNING)
            supplyManager.cancel(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("You Lose!").setMessage("You are dead 3 times").setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String usernameHex = Hex.convertStringToHex(username);
                //make Delete Request
                String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/playersInGame/" + usernameHex;
                new DeleteUserInGame(MapsActivity.this, getApplicationContext(), username).execute(httpUrl);
            }
        }).create().show();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //when map is ready, location and some settings are enabled
        mMap = map;

        enableMyLocation();
        mUiSettings = mMap.getUiSettings();

        mUiSettings.setCompassEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);

    }

    //Enables the My Location layer if the fine location permission has been granted.
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    private void getWinners() {
        //when the game is over, the game must be deleted from the server
        textViewTime.setText("");
        mapSupportTask.cancel(true);
        checkNPlayersTask.cancel(true);
        btThread.stopThread();

        if(supplyCreatorTask.getStatus() == AsyncTask.Status.RUNNING)
            supplyCreatorTask.cancel(true);       //stop these asynctask if they are started
        if(supplyManager.getStatus() == AsyncTask.Status.RUNNING)
            supplyManager.cancel(true);

        String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/playersingame/" + usernameHex;
        //make Get Request
        new GameOverManagement(MapsActivity.this, getApplicationContext(), username, nameGame, creator, String.valueOf(lives)).execute(httpUrl);
    }

    // class used to start the game timer
    private class CounterClass extends CountDownTimer {
        //timer of the game, when it ends, game finishes
        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long millis = millisUntilFinished;
            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            textViewTime.setText(hms);
        }

        @Override
        public void onFinish() {
            sendMessageBluetooth("2");
            if(deathMatch)
                getWinners();

            if(!deathMatch){
                mapSupportTask.cancel(true);
                checkNPlayersTask.cancel(true);
                btThread.stopThread();

                if(supplyCreatorTask!=null)
                    supplyCreatorTask.cancel(true);       //stop these asynctask if they are started
                if(supplyManager!=null)
                    supplyManager.cancel(true);

                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Game Over").setMessage("Details of Game:\n"+ "\n\t\t\tPlayer: " + username + "\n\t\t\tDead: " + dead + "\n\t\t\tRemaining Shot: " + shot + "/" + totalShot + "\n\t\t\tGame: " + nameGame + "\n\t\t\tTime: " + duration + " min" + "\n\t\t\tDate: " + date + "\n\t\t\tLocation: " + location
                ).setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(creator.equals(username)){
                            String nameGameHex = Hex.convertStringToHex(nameGame);
                            //make Delete Request
                            String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/game/" + nameGameHex;
                            new DeleteGame(MapsActivity.this, getApplicationContext(), username).execute(httpUrl);
                        }
                        else{
                            String usernameHex = Hex.convertStringToHex(username);
                            //make Delete Request
                            String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/playersInGame/" + usernameHex;
                            new DeleteUserInGame(MapsActivity.this, getApplicationContext(), username).execute(httpUrl);
                        }
                    }
                }).create().show();
            }
        }

    }

    // method used to comunicate with ArduinoSide
    private void sendMessageBluetooth(String message) {
        BluetoothSocket btSocket = GlobalValue.getInstance().getSocket();
        OutputStream outStream;

        if(btSocket!=null) {
            try {
                outStream = btSocket.getOutputStream();
                byte[] msgBuffer = message.getBytes();
                outStream.write(msgBuffer);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Displays a dialog with error message explaining that the location permission is missing.
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    @Override
    public void onBackPressed() {
        //it disables the back button of the smartphone in this Activity
    }

    public void startAsyncTasks(){
        String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/playersInGame/" + nameGame;
        mapSupportTask = new MapSupportTask(handler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, httpUrl);
        //used to manage positions of enemies

        //btTask = new BluetoothTask(handler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        btThread = new BluetoothThread(handler);
        btThread.start();


        httpUrl = "https://futurewarfare-cruizer.c9users.io/api/playersInGame/" + nameGame;
        checkNPlayersTask = new NumberOfWaitingPlayers(MapsActivity.this, handler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, httpUrl);

    }
}
