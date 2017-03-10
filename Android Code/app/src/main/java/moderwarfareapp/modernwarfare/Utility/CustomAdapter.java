package moderwarfareapp.modernwarfare.Utility;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import moderwarfareapp.modernwarfare.R;
import moderwarfareapp.modernwarfare.Utility.Item;

/**
 * Created by andrea on 30/04/16.
 */

//CustomAdapter is used to create the ListView
public class CustomAdapter extends ArrayAdapter{
    private List list = new ArrayList();    //arraylist populated by items

    public CustomAdapter(Context context, int resource){
        super(context,resource);
    }

    //function add, to add an Item in the list
    public void add(Item object) {
        super.add(object);
        list.add(object);
    }

    //getCount() returns the size of the list
    public int getCount(){
        return list.size();
    }

    //return the item in "position" position
    public Object getItem(int position){
        return list.get(position);
    }

    //getView populates the list with items
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemHolder itemHolder;
        if(row == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.row_layout, parent,false);
            itemHolder = new ItemHolder();
            itemHolder.game = (TextView) row.findViewById(R.id.tvGame);
            itemHolder.creator = (TextView) row.findViewById(R.id.tvCreator);
            itemHolder.location= (TextView) row.findViewById(R.id.tvLocation);
            itemHolder.date = (TextView) row.findViewById(R.id.tvDate);
            itemHolder.duration = (TextView) row.findViewById(R.id.tvDuration);
            itemHolder.players = (TextView) row.findViewById(R.id.tvPlayers);
            itemHolder.start= (TextView) row.findViewById(R.id.tvStart);
            itemHolder.kindOfGame = (TextView) row.findViewById(R.id.kindOfGame);
            row.setTag(itemHolder);
        }
        else
            itemHolder = (ItemHolder) row.getTag();

        Item items = (Item) this.getItem(position);
        itemHolder.game.setText(items.getGame());
        itemHolder.creator.setText(items.getCreator());
        itemHolder.location.setText(items.getLocation());
        itemHolder.date.setText(items.getDate());
        itemHolder.players.setText(items.getPlayers());
        itemHolder.start.setText(items.getStart());
        itemHolder.duration.setText(items.getDuration());
        itemHolder.kindOfGame.setText(items.getKindOfGame());
        return row;
    }

    static class ItemHolder{
        TextView game, kindOfGame, creator, location, date, players, start, duration;
    }
}
