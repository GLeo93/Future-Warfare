package moderwarfareapp.modernwarfare.AsyncTasks;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import moderwarfareapp.modernwarfare.Utility.GlobalValue;

/**
 * Created by andrea on 28/07/16.
 */
public class BluetoothTask extends AsyncTask<Void, Integer, String> {
    private Handler handler;            //handler required to exchange information with the main activity
    private BluetoothSocket btSocket;           //socket required for connection and exchange of messages
    private OutputStream outStream;             //required for sending of messages
    private InputStream inStream;               //required for receiving of messages
    private boolean firstIteration = true;      //required to send Arduino the kindOfGame


    public BluetoothTask(Handler handler) {
        this.handler = handler;
        btSocket = GlobalValue.getInstance().getSocket();
        if (btSocket != null) {
            try {
                inStream = btSocket.getInputStream();
                System.out.println("my " + inStream);
                outStream = btSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... arg0) {
        sendMessageBluetooth("1");

        byte[] buffer = new byte[512];
        int bytes;
        StringBuilder readMessage = new StringBuilder();
        while (!this.isCancelled()) {
            try {
                bytes = inStream.read(buffer);
                String readed = new String(buffer, 0, bytes);
                readMessage.append(readed);

                if (readed.contains("\n")) {
                    String recv = readMessage.substring(0, readMessage.length() - 2);
                    if (recv.equals("shot")) {
                        System.out.println("shot");
                        notifyMessage("shot");
                    } else if (recv.equals("hit")) {
                        System.out.println("hit");
                        notifyMessage("hit");
                    }
                    readMessage.setLength(0);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "success";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }


    //these methods exchange information with MapsActivity thanks to handler
    private void notifyMessage(String str) {
        //if i'm dead thread sends this message to MapsActivity
        Message msg = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putString(str, "");
        msg.setData(b);
        handler.sendMessage(msg);
    }

    //this method allow us to send a message via bluetooth
    private void sendMessageBluetooth(String message) {
        if (outStream == null) {
            return;
        }
        byte[] msgBuffer = message.getBytes();
        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
}

    /*
    @Override
    protected String doInBackground(Void... arg0) {
        while (!this.isCancelled()) {
            if(firstIteration) {        //if the game is just started, notify the kindOfGame
                firstIteration = false;
                if (GlobalValue.getInstance().getKindOfGame() == "Death Match"){
                    sendMessageBluetooth("4");
                }
                else {
                    sendMessageBluetooth("5");
                }
            }

            sendMessageBluetooth("1");          //with 1 i ask remaining number of shots
            String colpi = receiveMessageBluetooth();
            notifyMessageShots(colpi);

            sendMessageBluetooth("2");          //with 2 i ask if i'm dead: it response with 1 if i'm dead, 0 otherwise
            String morto = receiveMessageBluetooth();
            notifyMessageDead(morto);
        }
        return "success";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }


    //these methods exchange information with MapsActivity thanks to handler
    private void notifyMessageDead(String str) {
        //if i'm dead thread sends this message to MapsActivity
        Message msg = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("morto", ""+ str);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    private void notifyMessageShots(String str) {
        //thread sends this message to MapsActivity
        Message msg = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("colpi", ""+ str);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    //this method allow us to send a message via bluetooth
    private void sendMessageBluetooth(String message) {
        if (outStream == null) {
            return;
        }
        byte[] msgBuffer = message.getBytes();
        try{
            outStream.write(msgBuffer);
        }
        catch (IOException e){
            e.getStackTrace();
        }
    }

    //this method allow us to receive a message via bluetooth
    private String receiveMessageBluetooth() {
        try {
            Thread.sleep(2000);             //We wait a few seconds before request the response
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        byte[] buffer = new byte[512];
        int bytes;
        StringBuilder readMessage = new StringBuilder();
        String recv = "";
        boolean fine = true;
        while (fine) {
            try {
                Thread.sleep(1000);
                bytes = inStream.read(buffer);
                String readed = new String(buffer, 0, bytes);
                readMessage.append(readed);

                if (readed.contains("\n") || readed.contains("\0")) {
                    fine = false;
                    recv = readMessage.toString();
                    readMessage.setLength(0);
                }

            } catch (IOException e) {
                e.getStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return recv;
    }
}
*/