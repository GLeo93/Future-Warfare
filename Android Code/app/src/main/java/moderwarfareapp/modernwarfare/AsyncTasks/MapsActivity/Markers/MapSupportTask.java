package moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.Markers;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by andrea on 28/07/16.
 */
public class MapSupportTask extends AsyncTask<String, Void, String> {
    private Handler handler;            //handler required to exchange information with the main activity

    public MapSupportTask(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        while (!this.isCancelled()) {
            try {
                String json = getData(params[0]);
                //get all users
                notifyMessage("updateMap", json);

                Thread.sleep(6000);     //for each iteration thread sleeps for 6s, showing position of users on the map

                //after 6s a request to remove users coordinates on map is sent to MapsActivity
                notifyMessage("clear");

                Thread.sleep(15000);    //then, for 15s, no positions are shown in the map

            } catch (IOException ex) {
                return "Network error!";
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "success";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    private String getData(String urlPath) throws  IOException {
        StringBuilder result = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            //Initialize and config request, then connect to server
            URL url = new URL(urlPath);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);        /*milliseconds*/
            urlConnection.setConnectTimeout(10000);     /*milliseconds*/
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");   //set header
            urlConnection.connect();

            //Read data response from server
            InputStream inputStream = urlConnection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line).append("\n");
            }
        } finally {
            if(bufferedReader != null)
                bufferedReader.close();

        }
        return result.toString();
    }

    //this method exchanges information with CreatorWaitingArea thanks to handler
    private void notifyMessage(String mess, String str) {
        //when AsyncTask send a message to the Main activity, it must refresh the text with number of players
        Message msg = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putString(mess, str);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    private void notifyMessage(String mess) {
        //when AsyncTask send a message to the Main activity, it must refresh the text with number of players
        Message msg = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putString(mess, "");
        msg.setData(b);
        handler.sendMessage(msg);
    }
}