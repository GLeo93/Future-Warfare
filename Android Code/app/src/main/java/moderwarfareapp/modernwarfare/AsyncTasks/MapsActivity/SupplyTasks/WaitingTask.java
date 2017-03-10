package moderwarfareapp.modernwarfare.AsyncTasks.MapsActivity.SupplyTasks;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import moderwarfareapp.modernwarfare.Utility.GlobalValue;

/**
 * Created by andrea on 06/09/16.
 */
public class WaitingTask extends AsyncTask<Void, Integer, String> {
    private Handler handler;

    public WaitingTask(Handler handler){
        this.handler = handler;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... arg0) {
        try {
            Thread.sleep(30000); //30
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        GlobalValue.getInstance().setAbleToCreate(true);
        notifyMessageRemoveSupply();
        return "success";
    }

    private void notifyMessageRemoveSupply() {
        //when thread send this message to MapsActivity, it must insert enemies coordinates on the map
        Message msg = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("remove","");
        msg.setData(b);
        handler.sendMessage(msg);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

}
