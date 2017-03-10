package moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import moderwarfareapp.modernwarfare.Activity.UserMenu;
import moderwarfareapp.modernwarfare.Utility.GlobalValue;
import moderwarfareapp.modernwarfare.Utility.Hex;

/**
 * Created by andrea on 23/07/16.
 */
public class NumberOfWaitingPlayers extends AsyncTask<String, Void, String> {
    private Handler handler;            //handler required to exchange information with the main activity
    Activity linkedActivity;

    public NumberOfWaitingPlayers(Activity linkedActivity, Handler handler) {
        this.linkedActivity= linkedActivity;
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
                JSONArray jsonArray = new JSONArray(json);
                notifyMessage(jsonArray.length());

                Thread.sleep(1000);

            } catch (IOException ex) {
                return "Network error!";
            } catch (JSONException e) {
                e.printStackTrace();
                return "JSON error!";
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
    private void notifyMessage(int str) {
        //when AsyncTask send a message to the Main activiy, it must refresh the text with number of players
        Message msg = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putInt("players", str);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
}