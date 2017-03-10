package moderwarfareapp.modernwarfare.Activity;

import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v7.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import moderwarfareapp.modernwarfare.AsyncTasks.WaitingAreas.AddUserInGame;
import moderwarfareapp.modernwarfare.Utility.CustomAdapter;
import moderwarfareapp.modernwarfare.R;
import moderwarfareapp.modernwarfare.Utility.Hex;
import moderwarfareapp.modernwarfare.Utility.Item;


public class JoinInGame extends AppCompatActivity {

    private JSONArray jsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        //take fields from the previous Activity
        final String json = getIntent().getExtras().getString("json");
        final String username = getIntent().getExtras().getString("username");

        //management of the list, it will contain all the games not started yet
        ListView listView = (ListView) findViewById(R.id.list_Item);
        CustomAdapter customAdapter = new CustomAdapter(this, R.layout.row_layout);
        listView.setAdapter(customAdapter);

        //support variables, used to add items on listView
        String game, kindOfGame, creator, location, players, startDate, duration, startTime;
        Item item;

        try {
            //parsing the JSON and assigning game attributes to support variables
            jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jo = jsonArray.getJSONObject(i);
                game = Hex.convertHexToString(jo.getString("_id"));
                creator = "[".concat(jo.getString("creator")).concat("]");
                location = "Place: ".concat(jo.getString("location"));
                players = "Max Players: ".concat(jo.getString("players"));
                startDate = jo.getString("startDate");
                duration = "Duration: ".concat(jo.getString("duration")).concat(" min");
                startTime = "At: ".concat(jo.getString("startTime"));
                kindOfGame = jo.getString("kindOfGame");

                //item is created, then is added to the Adapter
                item = new Item(game, kindOfGame, creator, location, players, startDate, duration, startTime);
                customAdapter.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String nameGame = String.valueOf(((Item) parent.getItemAtPosition(position)).getGame());
                final String selectedKindOfGame = String.valueOf(((Item) parent.getItemAtPosition(position)).getKindOfGame());

                //if listView item is pressed, is shown this message
                AlertDialog.Builder builder = new AlertDialog.Builder(JoinInGame.this);
                builder.setMessage("Are you sure to join this game?").setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    //make POST Request
                    new AddUserInGame(JoinInGame.this, getApplicationContext(), username, nameGame, selectedKindOfGame, " ").execute("https://futurewarfare-cruizer.c9users.io/api/playersInGame");

                    }

                    //if the user reject, he returned to listView
                }).setNegativeButton(android.R.string.no, null).setIcon(android.R.drawable.ic_dialog_alert).show();
                AlertDialog dialog = builder.create();
            }
        });
    }
}