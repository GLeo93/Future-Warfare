package moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by andrea on 23/07/16.
 */
public class NumberOfPlayersInGame extends AsyncTask<String, Void, String> {
    private Handler handler;            //handler required to exchange information with the main activity

    public NumberOfPlayersInGame(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String json = getData(params[0]);
            JSONArray jsonArray = new JSONArray(json);
            notifyMessage(jsonArray.length());
        } catch (IOException ex) {
            return "Network error!";
        } catch (JSONException e) {
            e.printStackTrace();
            return "JSON error!";
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
    private void notifyMessage(int str) {
        //when AsyncTask send a message to the Main activiy, it must refresh the text with number of players
        Message msg = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putInt("players", str);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
}