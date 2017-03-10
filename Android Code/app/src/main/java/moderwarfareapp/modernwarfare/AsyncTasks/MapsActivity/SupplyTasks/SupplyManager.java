package moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.SupplyTasks;

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
public class SupplyManager extends AsyncTask<String, Void, String> {
    private Handler handler;            //handler required to exchange information with the main activity

    public SupplyManager(Handler handler) {
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

                JSONObject jo = new JSONObject(json);
                if(jo.isNull("latitude") || jo.isNull("longitude")){
                    Thread.sleep(1000);
                }
                else {
                    double supplyLatitude = jo.getDouble("latitude");
                    double supplyLongitude = jo.getDouble("longitude");
                    notifyMessageSupply(supplyLatitude, supplyLongitude);
                }
                Thread.sleep(1000);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
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

    private void notifyMessageSupply(double lat, double lon) {
        //when thread send this message to MapsActivity, it must insert the supply
        Message msg = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("supply","");
        b.putDouble("latitude",lat);
        b.putDouble("longitude",lon);
        msg.setData(b);
        handler.sendMessage(msg);
    }
}