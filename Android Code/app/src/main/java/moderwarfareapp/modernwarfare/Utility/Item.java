package moderwarfareapp.modernwarfare.Utility;

/**
 * Created by andrea on 30/04/16.
 */

//An item is a single field in the ListView
public class Item {
    private String game, kindOfGame, creator, location, players, date, duration, start;

    public Item(String game, String kindOfGame, String creator, String location, String players, String date, String duration, String start){
        this.game = game;
        this.kindOfGame = kindOfGame;
        this.creator = creator;
        this.location = location;
        this.players = players;
        this.date = date;
        this.duration = duration;
        this.start = start;
    }

    //get and set of necessary variables
    public String getGame(){
        return game;
    }

    public String getKindOfGame(){
        return kindOfGame;
    }

    public String getCreator() {return creator; }

    public String getLocation(){
        return location;
    }

    public String getPlayers(){
        return players;
    }

    public String getDate(){
        return date;
    }

    public String getDuration(){
        return duration;
    }

    public String getStart(){
        return start;
    }

    public void setGame(String game){
        this.game = game;
    }

    public void setCreator(String creator){
        this.creator= creator;
    }

    public void setLocation(String location){
        this.location= location;
    }

    public void setStart(String start){
        this.start= start;
    }
}
