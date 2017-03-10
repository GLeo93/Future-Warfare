package moderwarfareapp.modernwarfare.Utility;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import moderwarfareapp.modernwarfare.R;
import moderwarfareapp.modernwarfare.Activity.UserMenu;


public class DeviceList extends AppCompatActivity {
    private String name, username;                  //name and username fields of the user
    private Button btnPaired;                       //button used to load list of paired devices
    private ListView devicesList;                   //list to show all paired devices
    private String address = null;                  //mac address of current device of the list
    private ProgressDialog progress;                //dialog to show connecting message
    private BluetoothAdapter myBluetooth = null;    //to manage the bluetooth connection
    private BluetoothSocket btSocket = null;        //socket required for connection and exchange of messages
    private OutputStream outStream;                 //required for sending of messages
    private InputStream inStream;                   //required for receiving of messages

    private boolean isBtConnected = false;  //bluetooth is paired with arduino?
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //take fields from the previous Activity
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        btnPaired = (Button) findViewById(R.id.button);         //button used to load list of paired devices
        devicesList = (ListView) findViewById(R.id.listView);   //this list will contain all paired devices of smartphone

        //management of smartphone's bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if (myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth not working", Toast.LENGTH_LONG).show();
            finish();
        } else if (!myBluetooth.isEnabled()) {
            //if bluetooth is off, start the request to enable bluetooth
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PairedDevicesList();
            }
        });
        //when Paired Devices Button is pressed, is called the method "pairedDevicesList"

    }

    private void PairedDevicesList() {
        //management of paired devices list
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No paired devices", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        devicesList.setAdapter(adapter);
        devicesList.setOnItemClickListener(myListClickListener);

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            address = info.substring(info.length() - 17);
            //When a device is clicked, start the request to connect
            new ConnectBT().execute();
        }
    };


    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            //when a device is clicked for the pair, a preExecute message "connecting, please wait" is shown
            progress = ProgressDialog.show(DeviceList.this, "Connecting...", "Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {    //we try to connect with arduino
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                    outStream = btSocket.getOutputStream();
                    inStream = btSocket.getInputStream();
                }
            } catch (IOException e) {   //if something wrong here we catch exceptions, setting connectSuccess = false (used onPostExecute method)
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed");
                finish();
            } else {   //at the end of the pairing, if connection success
                isBtConnected = true;
                GlobalValue.getInstance().setSocket(btSocket);  //socket is set as a global value, accessible from all the project

                AlertDialog.Builder builder = new AlertDialog.Builder(DeviceList.this);
                builder.setTitle("Connected").setMessage("Press \"Ok\" to go back").setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(DeviceList.this, UserMenu.class);
                        intent.putExtra("username", username);
                        DeviceList.this.startActivity(intent);
                    }
                }).create().show();
                //a message of "successful connection" is shown and used is redirected in the UserMenu
            }
            progress.dismiss();
        }
    }
}