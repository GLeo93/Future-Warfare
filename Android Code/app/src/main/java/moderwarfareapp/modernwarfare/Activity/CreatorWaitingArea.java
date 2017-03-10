package moderwarfareapp.modernwarfare.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AnalogClock;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;

import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.DeleteGame;
import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.PutStartGame;
import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.NumberOfWaitingPlayers;
import moderwarfareapp.modernwarfare.R;
import moderwarfareapp.modernwarfare.Utility.GlobalValue;
import moderwarfareapp.modernwarfare.Utility.Hex;

public class CreatorWaitingArea extends AppCompatActivity {
    private static String nameGame;             //is the name of the game created by the user
    private TextView numberOfPlayersInGame;     //this textview shows number of user joined in the game
    private AsyncTask checkNPlayersTask;        //asynctask controls the number of the players joined in the game

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_creator);

        final AnalogClock clock = (AnalogClock) findViewById(R.id.analogClock);     //a clock is shown in this Activity
        final Button bDeleteGame = (Button) findViewById(R.id.bDeleteGame);
        final Button bStartGame = (Button) findViewById(R.id.bStartGame);

        //take fields from the previous Activity
        Intent intent = getIntent();
        nameGame = intent.getStringExtra("nameGame");
        final String username = intent.getStringExtra("username");
        final String kindOfGame = intent.getStringExtra("kindOfGame");
        GlobalValue.getInstance().setKindOfGame(kindOfGame);
        numberOfPlayersInGame = (TextView) findViewById(R.id.remaining);

        Handler handler = new MyHandler();
        String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/playersInGame/" + nameGame;

        //make Get Request
        checkNPlayersTask = new NumberOfWaitingPlayers(CreatorWaitingArea.this, handler).execute(httpUrl);


        bStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   //if startGame button is pressed, we are redirected in MapsActivity
                //AsyncTask is stopped
                checkNPlayersTask.cancel(true);

                String nameGameHex = Hex.convertStringToHex(nameGame);
                //make PUT Request
                String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/game/" + nameGameHex;
                new PutStartGame(CreatorWaitingArea.this, getApplicationContext(), username, nameGame, kindOfGame).execute(httpUrl);
            }
        });

        bDeleteGame.setOnClickListener(new View.OnClickListener() {     //user can delete the Game that he has created
            @Override
            public void onClick(View v) {
                //AsyncTask is stopped
                checkNPlayersTask.cancel(true);

                String nameGameHex = Hex.convertStringToHex(nameGame);
                //make Delete Request
                String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/game/" + nameGameHex;
                new DeleteGame(CreatorWaitingArea.this, getApplicationContext(), username).execute(httpUrl);
            }
        });

        //Share Dialog
        ShareButton fbShareButton = (ShareButton) findViewById(R.id.share_button);
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentTitle("Join with me in FutureWarfare!")
                .setContentDescription("Open the app and search the Game called '" + nameGame + "'!! It is a " + kindOfGame + "!!! Come on!")
                        .setContentUrl(Uri.parse("http://modernwarfareapp.altervista.org/"))
                        .setImageUrl(Uri.parse("http://modernwarfareapp.altervista.org/images/futurewarfare.png"))
                        .build();
        fbShareButton.setShareContent(content);




    }

    //this handler helps the execution of the AsyncTask
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            //this handler receives messages by AsyncTask
            Bundle bundle = msg.getData();
            if(bundle.containsKey("players")) {
                int value = bundle.getInt("players");
                numberOfPlayersInGame.setText("Current Players: " + value);
            }
        }
    }

    @Override
    public void onBackPressed() {
        //it disables the back button of the smartphone in this Activity
    }
}
