package moderwarfareapp.modernwarfare.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;

import java.util.Scanner;

import moderwarfareapp.modernwarfare.AsyncTasks.DeleteTemp;
import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.AddNewGame;
import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.DeleteUserInGame;
import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.GetAllGames;
import moderwarfareapp.modernwarfare.R;
import moderwarfareapp.modernwarfare.Utility.DeviceList;
import moderwarfareapp.modernwarfare.Utility.GlobalValue;
import moderwarfareapp.modernwarfare.Utility.Hex;

public class UserMenu extends AppCompatActivity {
    private boolean connected = false;
    TextView welcomeMessage;
    ImageView photoFb;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        connected = GlobalValue.getInstance().getConnected();   //the smartphone is connected with arduino's bluetooth?

        photoFb= (ImageView) findViewById(R.id.profile_image);
        welcomeMessage = (TextView) findViewById(R.id.tvWelcomeMsg);                //this field will contain the welcome message for the user
        final Button bcreateGame = (Button) findViewById(R.id.createGame);          //button used for the creation of the game
        final Button bjoinGame = (Button) findViewById(R.id.joinGame);              //button used to join in a existent game
        final Button bluetoothButton = (Button) findViewById(R.id.bluetoothButton); //button used to connect with arduino's bluetooth

        Intent intent = getIntent();    //take fields from the previous Activity

        final String nameGame = intent.getStringExtra("nameGame");

        GetUserInfo();      //get all data of the user from facebook

        bcreateGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   //if createGame button is pressed, we are redirected in CreateGame
                if (connected) {    //but only if we are connected
                    Intent createGameIntent = new Intent(UserMenu.this, CreateGame.class);
                    createGameIntent.putExtra("username", username);    //we give username and name to the next Activity
                    UserMenu.this.startActivity(createGameIntent);
                } else {    //error message
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserMenu.this);
                    builder.setMessage("Please connect the Gun").setNegativeButton("Retry", null).create().show();
                }
            }
        });

        bjoinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   //if joinGame button is pressed, we are redirected in JoinInGame
                if (connected) {    //but only if we are connected
                    //make Get Request
                    new GetAllGames(UserMenu.this, username).execute("https://futurewarfare-cruizer.c9users.io/api/getJoinGames/"+ username);
                }
                else{ //if we are not connected, this message is shown
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserMenu.this);
                    builder.setMessage("Please connect the Gun").setNegativeButton("Retry", null).create().show();
                }
            }
        });

        //if the bluetooth button is pressed, we are redirected in DeviceList
        bluetoothButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent joinGameIntent = new Intent(UserMenu.this, DeviceList.class);
                joinGameIntent.putExtra("username", username);
                UserMenu.this.startActivity(joinGameIntent);
                //redirected to DeviceList, passing it name and username
            }
        });
    }

    @Override
    public void onBackPressed() {
        //it disables the back button of the smartphone in this Activity
    }

    private void GetUserInfo(){
        //this code will help us to obtain information from facebook, if
        //need some other field which not show here, please refer to https://developers.facebook.com/docs/graph-api/using-graph-api/
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        // Application code
                        try{
                            String name = object.getString("name");

                            Scanner scanner = new Scanner(name);
                            scanner.useDelimiter("\\W");
                            String firstName= scanner.next();
                            String lastName = scanner.next();
                            username = firstName.substring(0,4) + lastName.substring(0,4);

                            welcomeMessage.setText("Welcome " + name + "!");

                            if (object.has("picture")) {
                                String profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                Picasso.with(UserMenu.this).load(profilePicUrl).into(photoFb);
                            }

                            String usernameHex = Hex.convertStringToHex(username);
                            String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/gameandsupply/"+ usernameHex;
                            new DeleteTemp(UserMenu.this, username).execute(httpUrl);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.fblogout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(UserMenu.this);
            builder.setTitle("Warning").setMessage("Are you sure to log out?").setPositiveButton("YES", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    LoginManager.getInstance().logOut();
                    UserMenu.this.startActivity(new Intent(UserMenu.this, Login.class));
                }
            }).setNegativeButton("NO", null).create().show();
            return true;
        }
        else if(id == R.id.remove){
            AlertDialog.Builder builder = new AlertDialog.Builder(UserMenu.this);
            builder.setTitle("Warning").setMessage("Have you got any problems with games? Reset settings!").setPositiveButton("YES", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String usernameHex = Hex.convertStringToHex(username);
                    //make Delete Request
                    String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/playersInGame/" + usernameHex;
                    new DeleteUserInGame(UserMenu.this, getApplicationContext(), username).execute(httpUrl);

                }
            }).setNegativeButton("NO", null).create().show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
