package common;

import java.io.Serializable;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Game implements Serializable {
    private transient ConcurrentHashMap<String, Socket> socketMap;  // List 해도 되지만 혹시 귓말 등 추가 때문에 Map 사용

    private String gameId;
    private String author;
    private String title;
    private int likes;
    private Candidate candidate1;
    private Candidate candidate2;
    private Map<Integer, Comment> comments;

    public Game(String gameId, String author, String title) {
        this.socketMap = new ConcurrentHashMap<>();
        this.comments = new ConcurrentHashMap<>();

        this.gameId = gameId;
        this.author = author;
        this.title = title;
        this.likes = 0;
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

    public void addVote(int candidateNumber) {
        if(candidateNumber == 1) candidate1.addVote();
        if(candidateNumber == 2) candidate2.addVote();
    }

    public void addComment(String writer, String content) {
        Comment comment = Comment.from(comments.size(), writer, content);
        comments.put(comment.getCommentId(), comment);
    }

    public void initSocketMap() {
        this.socketMap = new ConcurrentHashMap<>();
    }

    public void setCandidate1(Candidate candidate1) {
        this.candidate1 = candidate1;
    }

    public void setCandidate2(Candidate candidate2) {
        this.candidate2 = candidate2;
    }

    public Candidate getCandidate1() {
        return candidate1;
    }

    public Candidate getCandidate2() {
        return candidate2;
    }

    public int getVotesNum(){
        return candidate1.getVotes() + candidate2.getVotes();
    }

    public Comment getCommentById(int commentId) {
        return comments.get(commentId);
    }
}
