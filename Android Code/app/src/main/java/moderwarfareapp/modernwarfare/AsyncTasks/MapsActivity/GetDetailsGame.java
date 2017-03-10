package moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by andrea on 23/07/16.
 */
public class GetDetailsGame extends AsyncTask<String, Void, String> {
    Activity linkedActivity;
    Handler handler;
    Context context;


    public GetDetailsGame(Activity linkedActivity, Context context, Handler handler) {
        this.linkedActivity= linkedActivity;
        this.handler = handler;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            return getData(params[0]);
        } catch (IOException e) {
            return "error";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if(result.equals("error"))
            Toast.makeText(context, "Error loading details of the game", Toast.LENGTH_SHORT).show();
        else
            notifyDataGameMessage(result);
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
    private void notifyDataGameMessage(String mess) {
        //when AsyncTask send a message to the Main activity, it must refresh the text with number of players
        Message msg = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("DetailGame", mess);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
}