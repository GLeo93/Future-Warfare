package moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.SupplyTasks;

import android.os.AsyncTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import moderwarfareapp.modernwarfare.Utility.GlobalValue;
import moderwarfareapp.modernwarfare.Utility.Hex;

/**
 * Created by andrea on 28/07/16.
 */
public class SupplyCreatorTask extends AsyncTask<String, Void, String> {
    private String myUsername;
    private LatLng myPosition;
    private Handler handler;            //handler required to exchange information with the main activity

    public SupplyCreatorTask(Handler handler, String myUsername) {
        this.handler = handler;
        this.myUsername = myUsername;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        while (!this.isCancelled()) {
            try {
                Thread.sleep(1000);      //thread sleeps 1s before requesting the "ability" to crate a supply
            boolean ableCreator = GlobalValue.getInstance().getAbleToCreate();  //boolean true to default
            if(ableCreator) {   //ableCreator allow the possibility to create or not a supply
                String json = getData(params[0]);
                JSONObject jsonObject = new JSONObject(json);
                String userAble = Hex.convertHexToString(jsonObject.getString("nextCreator"));    //real user authorized to create a supply
                if (userAble.equals(myUsername)) {      //if we are the user authorized, then
                    int randomvalue = (int) (Math.random() * 1000);     //we calculate a random number in range (1,100)
                    if (randomvalue > 0) {      //here you can modify the probability to create a supply
                        GlobalValue.getInstance().setAbleToCreate(false);           //current user is unauthorized to create a new supply

                        myPosition = GlobalValue.getInstance().getMyPosition();
                        //here we create a supply, in a random position in the map (near us) thanks this increment
                        double supplementLatitude = getRandomIncrement();
                        double supplementLongitude = getRandomIncrement();

                        double supplyLatitude = myPosition.latitude + supplementLatitude;
                        double supplyLongitude = myPosition.longitude + supplementLongitude;
                        notifyMessage(supplyLatitude, supplyLongitude);
                    }
                }
            }
            } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
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

    public double getRandomIncrement(){
        int nMin = 10; // numero minimo
        int nMax = 19; // numero massimo
        int tot = ((nMax-nMin) + 1);
        Random random = new Random();
        int temp_result = random.nextInt(tot) + nMin;
        String result = "0.00" + temp_result;
        double increment = Double.parseDouble(result);

        if((int)(Math.random()*10) < 5)
            increment*= -1;

        return increment;
    }


    private void notifyMessage(double lat, double lon) {
        //when thread send this message to MapsActivity, it must insert the supply
        Message msg = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("updateCoordinatesSupply","");
        b.putString("latitude",Double.toString(lat));
        b.putString("longitude",Double.toString(lon));
        msg.setData(b);
        handler.sendMessage(msg);
    }
}