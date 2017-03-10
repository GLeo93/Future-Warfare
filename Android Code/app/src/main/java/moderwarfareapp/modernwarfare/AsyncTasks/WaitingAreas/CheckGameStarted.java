package moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas;

import android.app.Activity;
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
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by andrea on 28/07/16.
 */
public class CheckGameStarted extends AsyncTask<String, Void, String> {
    private Handler handler;            //handler required to exchange information with the main activity
    Activity linkedActivity;
    boolean wait;

    public CheckGameStarted(Activity linkedActivity, Handler handler) {
        this.linkedActivity= linkedActivity;
        this.handler = handler;
        wait = true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        while (wait) {

            try {
                String json = getData(params[0]);

                JSONObject jo = new JSONObject(json);
                String started = jo.getString("started");
                if(started.equals("1")){
                    wait = false;
                    notifyStartMessage();
                }

                else
                    Thread.sleep(1000);

            } catch (IOException ex) {
                return "Network error!";
            } catch (JSONException e) {
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
    private void notifyStartMessage() {
        //when AsyncTask send a message to the Main activity, it must refresh the text with number of players
        Message msg = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("start", "");
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
}