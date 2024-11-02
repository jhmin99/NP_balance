package game;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Game implements Serializable {
    private transient ConcurrentHashMap<String, Socket> socketMap;  // List 해도 되지만 혹시 귓말 등 추가 때문에 Map 사용

    private String gameId;
    private String author;
    private String title;
    private int likes;
    private List<Candidate> candidates;

    public Game(String gameId, String author, String title) {
        this.socketMap = new ConcurrentHashMap<>();

        this.gameId = gameId;
        this.author = author;
        this.title = title;
        this.likes = 0;
        this.candidates = new ArrayList<>();
    }

    public ConcurrentHashMap<String, Socket> getSocketMap() {
        System.out.println("소켓 맵 가져감");
        return socketMap;
    }

    public String getGameId() {
        return gameId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }   // User - name

    public int getLikes() {
        return likes;
    }

    public void like() {
        likes++;
    }   // like -> 낙장불입 취소 불가능

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void addCandidate(Candidate candidate) {
        candidates.add(candidate);
    }

    public void addVote(int candidateNumber) {
        candidates.get(candidateNumber - 1).addVote();
    }

    public void initSocketMap() {
        this.socketMap = new ConcurrentHashMap<>();
    }
}
