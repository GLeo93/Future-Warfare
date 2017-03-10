package moderwarfareapp.modernwarfare.Utility;

import android.bluetooth.BluetoothSocket;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
/**
 * Created by andrea on 13/05/16.
 */

//this class contains fields accessible from all project
public class GlobalValue {
    private boolean connected = false;      //used to ensures the connection
    private boolean ableToCreate = true;    //used to enable the "nextCreator" to create a supply on the map
    private BluetoothSocket socket;     //socket required for connection or exchange messages
    private String kindOfGame;      //contains the kind of game
    private String username;        //contains the username of the player
    private LatLng myPosition;      //

    GlobalValue() {
    }

    private static GlobalValue _instance = null;

    public static synchronized GlobalValue getInstance() {
        if (_instance == null)
            _instance = new GlobalValue();
        return _instance;
    }

    //return true if the phone is connected to Arduino
    public boolean getConnected() {
        return connected;
    }

    //return the socket to comunicate
    public BluetoothSocket getSocket() {
        return socket;
    }

    //set the socket to comunicate
    public void setSocket(BluetoothSocket socket) {
        this.socket = socket;
        connected = true;
    }

    //return the username of the player
    public String getUsername() {
        return username;
    }

    //set the username of the player
    public void setUsername(String username) {
        this.username = username;
    }

    //return the currently kind of game
    public String getKindOfGame() {
        return kindOfGame;
    }

    //set the kind if the game based on the radioButton chosen
    public void setKindOfGame(String kindOfGame) {
        this.kindOfGame = kindOfGame;
    }

    //return the LatLon that is a structure contains the latitude and the longitude of the player position
    public LatLng getMyPosition() {
        return myPosition;
    }

    //set the latitude and the longitude of the player into a LatLng structure
    public void setMyPosition(LatLng myPosition) {
        this.myPosition = myPosition;
    }

    //return true if the creator is enabled
    public boolean getAbleToCreate() {
        return ableToCreate;
    }

    //set the ableToCreate variable, allow or not to create
    public void setAbleToCreate(boolean ableToCreate) {
        this.ableToCreate = ableToCreate;
    }

    // close the connection to the socket
    public synchronized void closeSocket(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
