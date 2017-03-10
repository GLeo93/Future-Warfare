package moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import moderwarfareapp.modernwarfare.Activity.CreatorWaitingArea;
import moderwarfareapp.modernwarfare.Utility.Hex;
import moderwarfareapp.modernwarfare.Activity.PlayersWaitingArea;

/**
 * Created by andrea on 23/07/16.
 */
public class AddUserInGame extends AsyncTask<String, Void, String> {
    Activity linkedActivity;
    Context context;
    private String username;
    private String nameGame;
    private String kindOfGame;
    private String creator;

    public AddUserInGame(Activity linkedActivity, Context context, String username, String nameGame, String kindOfGame, String creator) {
        this.linkedActivity= linkedActivity;
        this.username = username;
        this.nameGame = nameGame;
        this.kindOfGame = kindOfGame;
        this.creator = creator;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            return postData(params[0]);
        }
        catch(IOException ex){
            return "Network error!";
        }
        catch(JSONException ex){
            return "Invalid Data!";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if(result.equals("error"))
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        else {
            Intent intent;
            if(creator.equals(username))
                intent = new Intent(linkedActivity, CreatorWaitingArea.class);
            else
                intent = new Intent(linkedActivity, PlayersWaitingArea.class);
            intent.putExtra("nameGame",nameGame);
            intent.putExtra("username",username);
            intent.putExtra("kindOfGame", kindOfGame);
            Toast.makeText(context, "Join Player Success", Toast.LENGTH_SHORT).show();
            linkedActivity.startActivity(intent);
            //redirected to CreatorWaitingArea, passing it name username and the name of the game just created
        }
    }

    private String postData(String urlPath) throws  IOException, JSONException {
        StringBuilder result = new StringBuilder();
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        try {
            //Create data to send to server
            JSONObject dataToSend = new JSONObject();
            String usernameHex = Hex.convertStringToHex(username);
            dataToSend.put("_id", usernameHex);
            dataToSend.put("nameGame", nameGame);
            dataToSend.put("lives", "0");


            //Initialize and config request, then connect to server
            URL url = new URL(urlPath);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);        /*milliseconds*/
            urlConnection.setConnectTimeout(10000);     /*milliseconds*/
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);            /*enable output (body data)*/
            urlConnection.setRequestProperty("Content-Type", "application/json");   //set header
            urlConnection.connect();

            //Write data into server
            OutputStream outputStream = urlConnection.getOutputStream();
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            bufferedWriter.write(dataToSend.toString());
            bufferedWriter.flush();

            if(urlConnection.getResponseCode() == 201) {
                //Read data response from server
                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }
            else
                result.append("error");
        } finally {
            if(bufferedReader!= null)
                bufferedReader.close();
            if(bufferedWriter!= null)
                bufferedWriter.close();
        }
        return result.toString();
    }
}