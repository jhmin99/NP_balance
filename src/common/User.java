package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String name;
    private String password;
    private List<String> createdGameIds;
    private List<String> playedGameIds;
    private int totalLikes;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
        this.createdGameIds = new ArrayList<>();
        this.playedGameIds = new ArrayList<>();
        this.totalLikes = 0;
    }

    public void addCreatedGameId(String gameId){
        createdGameIds.add(gameId);
    }

    public void addPlayedGameId(String gameId){
        playedGameIds.add(gameId);
    }

    public void addLike(){
        totalLikes++;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getCreatedGameIds() {
        return createdGameIds;
    }

    public List<String> getPlayedGameIds() {
        return playedGameIds;
    }

    public int getTotalLikes() {
        return totalLikes;
    }
}
