package moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import moderwarfareapp.modernwarfare.Activity.UserMenu;

/**
 * Created by andrea on 23/07/16.
 */
public class DeleteUserInGame extends AsyncTask<String, Void, String> {
    Activity linkedActivity;
    Context context;
    private String username;

    public DeleteUserInGame(Activity linkedActivity, Context context, String username) {
        this.linkedActivity= linkedActivity;
        this.username = username;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try{
            return deleteData(params[0]);
        } catch (IOException ex){
            return "Network error!";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (result.equals("Delete Failed!"))
            Toast.makeText(context, "Unsubscribe Failed!", Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(context, "Unsubscribed", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(linkedActivity, UserMenu.class);
        intent.putExtra("username", username);
        linkedActivity.startActivity(intent);
        linkedActivity.startActivity(intent);
        //redirected to UserMenu, passing it name and username
    }

    private String deleteData(String urlPath) throws IOException{
        //Initialize and config request, then connect to server
        URL url = new URL(urlPath);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setReadTimeout(10000);        /*milliseconds*/
        urlConnection.setConnectTimeout(10000);     /*milliseconds*/
        urlConnection.setRequestMethod("DELETE");
        urlConnection.setRequestProperty("Content-Type", "application/json");   //set header
        urlConnection.connect();

        //Check delete successful or not
        if(urlConnection.getResponseCode()==204)
            return "Delete Successfully!";
        else
            return "Delete Failed!";
    }
}
