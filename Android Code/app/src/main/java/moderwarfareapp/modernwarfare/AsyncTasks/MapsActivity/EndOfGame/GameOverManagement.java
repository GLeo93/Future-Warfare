package moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.EndOfGame;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import moderwarfareapp.modernwarfare.Utility.Hex;

/**
 * Created by andrea on 23/07/16.
 */
public class GameOverManagement extends AsyncTask<String, Void, String> {
    ProgressDialog progressDialog;
    Activity linkedActivity;
    Context context;
    private String username;
    private String nameGame;
    private String creator;
    private String lives;

    public GameOverManagement(Activity linkedActivity, Context context, String username, String nameGame, String creator, String lives) {
        this.linkedActivity= linkedActivity;
        this.username = username;
        this.nameGame = nameGame;
        this.creator = creator;
        this.lives = lives;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(linkedActivity);
        progressDialog.setMessage("Loading the score...");
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        try{
            return putData(params[0]);
        } catch (IOException ex){
            return "Network error!";
        } catch (JSONException ex){
            return "Invalid Data!";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if(result.equals("Update Failed!"))
            Toast.makeText(context, "Error on updating lives", Toast.LENGTH_SHORT).show();
        else {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //cancel progress dialog
            if(progressDialog != null){
                progressDialog.dismiss();
            }

            String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/winners/" + nameGame;
            //make Get Request
            new GetRanking(linkedActivity, context, nameGame, creator, username).execute(httpUrl);
        }
    }

    private String putData(String urlPath) throws IOException, JSONException{
        BufferedWriter bufferedWriter = null;

        try {
            //Create data to update
            JSONObject dataToSend = new JSONObject();
            String usernameHex = Hex.convertStringToHex(username);
            dataToSend.put("_id", usernameHex);
            dataToSend.put("lives", lives);

            //Initialize and config request, then connect to server
            URL url = new URL(urlPath);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);        /*milliseconds*/
            urlConnection.setConnectTimeout(10000);     /*milliseconds*/
            urlConnection.setRequestMethod("PUT");
            urlConnection.setDoOutput(true);            /*enable output (body data)*/
            urlConnection.setRequestProperty("Content-Type", "application/json");   //set header
            urlConnection.connect();

            //Write data into server
            OutputStream outputStream = urlConnection.getOutputStream();
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            bufferedWriter.write(dataToSend.toString());
            bufferedWriter.flush();

            //Check update successful or not
            if(urlConnection.getResponseCode()==200)
                return "Update Successfully!";
            else
                return "Update Failed!";

        } finally {
            if (bufferedWriter != null)
                bufferedWriter.close();
        }
    }
}
