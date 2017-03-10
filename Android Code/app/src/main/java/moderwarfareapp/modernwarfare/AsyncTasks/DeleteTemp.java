package moderwarfareapp.modernwarfare.AsyncTasks;

import android.app.Activity;
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

import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.DeleteUserInGame;
import moderwarfareapp.modernwarfare.Utility.Hex;

/**
 * Created by andrea on 23/07/16.
 */
public class DeleteTemp extends AsyncTask<String, Void, String> {
    Activity linkedActivity;
    private String username;

    public DeleteTemp(Activity linkedActivity, String username) {
        this.linkedActivity = linkedActivity;
        this.username = username;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            return deleteData(params[0]);
        } catch (IOException ex) {
            return "Network error!";
        } catch (JSONException e) {
            return "Invalid Data!";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    private String deleteData(String urlPath) throws IOException, JSONException {
        BufferedWriter bufferedWriter = null;

        try {
            //Create data to delete
            JSONObject dataToSend = new JSONObject();
            dataToSend.put("username", username);


            //Initialize and config request, then connect to server
            URL url = new URL(urlPath);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);        /*milliseconds*/
            urlConnection.setConnectTimeout(10000);     /*milliseconds*/
            urlConnection.setRequestMethod("DELETE");
            urlConnection.setDoOutput(true);            /*enable output (body data)*/
            urlConnection.setRequestProperty("Content-Type", "application/json");   //set header
            urlConnection.connect();

            //Write data into server
            OutputStream outputStream = urlConnection.getOutputStream();
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            bufferedWriter.write(dataToSend.toString());
            bufferedWriter.flush();

            //Check update successful or not
            if (urlConnection.getResponseCode() == 200)
                return "Update Successfully!";
            else
                return "Update Failed!";

        } finally {
            if (bufferedWriter != null)
                bufferedWriter.close();
        }
    }
}
