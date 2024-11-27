package server;

import common.Comment;
import common.Game;

import java.io.*;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    private static final String GAME_FILE = "Game.dat";

    private ConcurrentHashMap<String, Game> games;
    private ConcurrentHashMap<Socket, ObjectOutputStream> outputStreamMap;

    public GameManager() {
        games = loadGames();
        if (games == null) games = new ConcurrentHashMap<>();
        for(Game game : games.values()){
            game.initSocketMap();
        }
        this.outputStreamMap = new ConcurrentHashMap<>();
    }

    public ObjectOutputStream getOOS(Socket socket){
        return outputStreamMap.get(socket);
    }

    public ObjectOutputStream addOOS(Socket socket, ObjectOutputStream objectOutputStream){
        return outputStreamMap.put(socket, objectOutputStream);
    }

    private ConcurrentHashMap<String, Game> loadGames() {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(GAME_FILE))) {
            return (ConcurrentHashMap<String, Game>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Game file not found or empty. Starting fresh.");
            return null;
        }
    }

    private void saveGames() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(GAME_FILE))) {
            oos.writeObject(games);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConcurrentHashMap<String, Game> getAllGames(){
        return games;
    }

    public void addGame(Game game) {
        games.put(game.getGameId(), game);
        saveGames();
    }

    public void addLikeToGame(String gameId) {
        Game game = findGameById(gameId);
        if (game != null) {
            game.like();
            saveGames();
        }
    }

    public void addVote(String gameId, int candidateNumber) {
        Game game = findGameById(gameId);
        if (game != null) {
            game.addVote(candidateNumber);
            saveGames();
        }
    }

    public void addComment(String gameId, String writer, String message){
        Game game = findGameById(gameId);
        if (game != null) {
            game.addComment(writer, message);
            saveGames();
        }
    }

    public Game findGameById(String gameId) {
        return games.get(gameId);
    }

    public ConcurrentHashMap<String, Socket> getSocketMapById(String gameId){
        Game game = findGameById(gameId);
        return game.getSocketMap();
    }

    public String generateGameId(){         // 음 뺄까
        return UUID.randomUUID().toString();
    }

    public void addLikeComment(String gameId, int commentId) {
        Game game = findGameById(gameId);
        Comment comment = game.getCommentById(commentId);
        if (comment != null) {
            comment.addLike();
            saveGames();
        }
    }
}
