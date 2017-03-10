package moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas;

import android.app.Activity;
import android.content.Context;
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

import moderwarfareapp.modernwarfare.Utility.Hex;

/**
 * Created by andrea on 23/07/16.
 */
public class AddNewGame extends AsyncTask<String, Void, String> {
    Activity linkedActivity;
    Context context;
    String nameGame;
    String kindOfGame;
    String location;
    String players;
    String startTime;
    String startDate;
    String duration;
    String creator;


    public AddNewGame(Activity linkedActivity, Context context, String nameGame, String kindOfGame, String location, String players, String start, String date, String duration, String creator) {
        this.linkedActivity= linkedActivity;
        this.context = context;
        this.nameGame = nameGame;
        this.kindOfGame = kindOfGame;
        this.location = location;
        this.players = players;
        this.startTime = start;
        this.startDate = date;
        this.duration = duration;
        this.creator = creator;
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
            Toast.makeText(context, "Creation Failed", Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(context, "Game Creation Successful", Toast.LENGTH_SHORT).show();
            //make POST Request
            new AddUserInGame(linkedActivity, context, creator, nameGame, kindOfGame, creator).execute("https://futurewarfare-cruizer.c9users.io/api/playersInGame");
        }
    }

    private String postData(String urlPath) throws  IOException, JSONException {
        StringBuilder result = new StringBuilder();
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        try {
            //Create data to send to server
            JSONObject dataToSend = new JSONObject();
            String nameGameHex = Hex.convertStringToHex(nameGame);
            dataToSend.put("_id", nameGameHex);
            dataToSend.put("kindOfGame", kindOfGame);
            dataToSend.put("location", location);
            dataToSend.put("players", players);
            dataToSend.put("startTime", startTime);
            dataToSend.put("startDate", startDate);
            dataToSend.put("duration", duration);
            dataToSend.put("creator", creator);
            dataToSend.put("started","0");

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