package moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import moderwarfareapp.modernwarfare.Activity.JoinInGame;

/**
 * Created by andrea on 23/07/16.
 */
public class GetAllGames extends AsyncTask<String, Void, String> {
    ProgressDialog progressDialog;
    Activity linkedActivity;
    String username;

    public GetAllGames(Activity linkedActivity, String username) {
        this.linkedActivity= linkedActivity;
        this.username = username;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressDialog = new ProgressDialog(linkedActivity);
        progressDialog.setMessage("Loading data...");
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            return getData(params[0]);
        }
        catch(IOException ex){
            return "Network error!";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        //cancel progress dialog
        if(progressDialog != null){
            progressDialog.dismiss();
        }

        Intent joinGameIntent = new Intent(linkedActivity, JoinInGame.class);
        joinGameIntent.putExtra("username", username);
        joinGameIntent.putExtra("json", result);
        linkedActivity.startActivity(joinGameIntent);
        //redirected to JoinInGame, passing it name and username and the Json of all the games
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
}