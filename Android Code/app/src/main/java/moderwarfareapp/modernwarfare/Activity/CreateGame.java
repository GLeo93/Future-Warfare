package moderwarfareapp.modernwarfare.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.AddNewGame;
import moderwarfareapp.modernwarfare.R;

public class CreateGame extends AppCompatActivity {
    private static  RadioGroup radioGroup;      //the radioGroup contains buttons "Friendly Match" and "Death Match"
    private static RadioButton radioButton;
    private static String kindOfGame;           //It's a string that will contains the kind of the game (Death Match or Friendly Match)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);

        final EditText etNameGame = (EditText) findViewById(R.id.nameGame);     //this field will contain the name of the game created by user
        final EditText etLocation = (EditText) findViewById(R.id.location);     //this field will contain the location of the game
        final EditText etPlayers = (EditText) findViewById(R.id.players);       //this field will contain number of players that user want in the game
        final EditText etStart = (EditText) findViewById(R.id.start);           //this field will contain the starting time
        final EditText etDate = (EditText) findViewById(R.id.date);             //this field will contain the starting date
        final EditText etDuration = (EditText) findViewById(R.id.duration);     //this field will contain the duration of the game
        final Button bfinalCreate = (Button) findViewById(R.id.bfinalCreate);   //button "creation of the game"
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);                //contains the two radioButton (one for Death Match and one for Friendly Match)

        Intent createGameIntent = getIntent();  //take fields from the previous Activity
        final String creator = createGameIntent.getStringExtra("username");

        bfinalCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  //if finalCreate button is pressed, we are redirected in CreatorWaitingArea
                //get all fields from the corresponding labels in the Activity
                final String nameGame = etNameGame.getText().toString();
                final String location = etLocation.getText().toString();
                final String players = etPlayers.getText().toString();
                final String start = etStart.getText().toString();
                final String date = etDate.getText().toString();
                final String duration = etDuration.getText().toString();

                if (nameGame.equals("") || location.equals("") || players.equals("") || start.equals("") || date.equals("") || duration.equals(""))
                    Toast.makeText(getApplicationContext(), "Please, compile all fields", Toast.LENGTH_SHORT).show();
                else {

                    //manage the radio group
                    int selected_id = radioGroup.getCheckedRadioButtonId();
                    radioButton = (RadioButton) findViewById(selected_id);
                    kindOfGame = radioButton.getText().toString();

                    //make POST Request
                    new AddNewGame(CreateGame.this, getApplicationContext(), nameGame, kindOfGame, location, players, start, date, duration, creator).execute("https://futurewarfare-cruizer.c9users.io/api/game");

                }
            }
        });
    }
}
