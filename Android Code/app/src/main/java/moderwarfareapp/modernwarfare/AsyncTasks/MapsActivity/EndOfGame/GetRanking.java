package moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.EndOfGame;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.DeleteGame;
import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.DeleteUserInGame;
import moderwarfareapp.modernwarfare.Utility.Hex;

/**
 * Created by andrea on 23/07/16.
 */
public class GetRanking extends AsyncTask<String, Void, String> {
    Activity linkedActivity;
    Context context;
    String nameGame;
    String username;
    String creator;

    public GetRanking(Activity linkedActivity, Context context, String nameGame, String creator, String username) {
        this.linkedActivity= linkedActivity;
        this.nameGame = nameGame;
        this.creator = creator;
        this.username = username;
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
        }
        catch(IOException ex){
            return "error";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if(result.equals("error")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(linkedActivity);
            builder.setMessage("Error on the ranking").setNegativeButton("Ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(creator.equals(username)){
                        String nameGameHex = Hex.convertStringToHex(nameGame);
                        //make Delete Request
                        String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/game/" + nameGameHex;
                        new DeleteGame(linkedActivity, context, username).execute(httpUrl);
                    }
                    else{
                        String usernameHex = Hex.convertStringToHex(username);
                        //make Delete Request
                        String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/playersInGame/" + usernameHex;
                        new DeleteUserInGame(linkedActivity, context, username).execute(httpUrl);
                    }
                    //when the game is over, user is redirected in UserMenu, passing username and name of the player
                }
            }).create().show();
        }
        else{
            try {
                JSONArray jsonArray = new JSONArray(result);
                String winnerUsername;
                String winnerLives;
                String totalString = "Ranking\n\t";
                int rank;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    winnerUsername = Hex.convertHexToString(jo.getString("_id"));
                    winnerLives = jo.getString("lives");
                    rank = i + 1;
                    totalString += rank + ") " + winnerUsername + " with " + winnerLives + " lives\n\t";
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(linkedActivity);
                builder.setTitle("You Win").setMessage(totalString).setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(creator.equals(username)){
                            String nameGameHex = Hex.convertStringToHex(nameGame);
                            //make Delete Request
                            String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/game/" + nameGameHex;
                            new DeleteGame(linkedActivity, context, username).execute(httpUrl);
                        }
                        else{
                            String usernameHex = Hex.convertStringToHex(username);
                            //make Delete Request
                            String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/playersInGame/" + usernameHex;
                            new DeleteUserInGame(linkedActivity, context, username).execute(httpUrl);
                        }
                        //when the game is over, user is redirected in UserMenu, passing username and name of the player
                    }
                }).create().show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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