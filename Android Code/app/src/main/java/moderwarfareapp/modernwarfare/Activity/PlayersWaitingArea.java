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

import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.CheckGameStarted;
import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.NumberOfWaitingPlayers;
import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.DeleteUserInGame;
import moderwarfareapp.modernwarfare.R;
import moderwarfareapp.modernwarfare.Utility.GlobalValue;
import moderwarfareapp.modernwarfare.Utility.Hex;

public class PlayersWaitingArea extends AppCompatActivity {
    private static  String nameGame, kindOfGame;
    private String username;
    private TextView numberOfPlayersInGame;     //this textview shows number of user joined in the game
    private AsyncTask checkGameStarted;         //asynctask controls that the field "started" is set to "1", then starts the game
    private AsyncTask checkNPlayersTask;        //asynctask controls the number of the players joined in the game

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_players);

        Intent intent = getIntent();

        username = intent.getStringExtra("username");
        nameGame = intent.getStringExtra("nameGame");
        kindOfGame = intent.getStringExtra("kindOfGame");

        final AnalogClock clock = (AnalogClock) findViewById(R.id.analogClock2);
        final Button bDeleteGame = (Button) findViewById(R.id.bUnsubscribe);
        numberOfPlayersInGame = (TextView) findViewById(R.id.currentPlayers);

        Handler handler = new MyHandler();

        String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/playersInGame/" + nameGame;
        //make Get Request
        checkNPlayersTask = new NumberOfWaitingPlayers(PlayersWaitingArea.this, handler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, httpUrl);

        String nameGameHex = Hex.convertStringToHex(nameGame);
        httpUrl = "https://futurewarfare-cruizer.c9users.io/api/game/" + nameGameHex;
        //make Get Request
        checkGameStarted = new CheckGameStarted(PlayersWaitingArea.this, handler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, httpUrl);

        bDeleteGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   //user can delete the "enrollment" of a game
                //AsyncTask are stopped
                checkNPlayersTask.cancel(true);
                checkGameStarted.cancel(true);

                String usernameHex = Hex.convertStringToHex(username);
                //make Delete Request
                String httpUrl = "https://futurewarfare-cruizer.c9users.io/api/playersInGame/" + usernameHex;
                new DeleteUserInGame(PlayersWaitingArea.this, getApplicationContext(), username).execute(httpUrl);

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

    //this handler helps the execution of the thread
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            //this handler receive messages by thread
            Bundle bundle = msg.getData();
            if (bundle.containsKey("start")) {
                //if the game is started, thread must be stopped, than user can be redirected to MapsActivity
                Intent startIntent = new Intent(PlayersWaitingArea.this, MapsActivity.class);
                startIntent.putExtra("nameGame", nameGame);
                startIntent.putExtra("username", username);
                startIntent.putExtra("kindOfGame", kindOfGame);
                GlobalValue.getInstance().setKindOfGame(kindOfGame);
                PlayersWaitingArea.this.startActivity(startIntent);

                //AsyncTasks are stopped
                checkNPlayersTask.cancel(true);

            }
            else if (bundle.containsKey("players")) {
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
