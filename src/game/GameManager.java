package game;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    private static final String GAME_FILE = "Game.dat";

    private List<Game> games;
    private ConcurrentHashMap<Socket, ObjectOutputStream> outputStreamMap;

    public GameManager() {
        games = loadGames();
        if (games == null) games = new ArrayList<>();
        for(Game game : games){
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

    private List<Game> loadGames() {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(GAME_FILE))) {
            return (List<Game>) ois.readObject();
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

    public List<Game> getAllGames(){
        return new ArrayList<>(games);
    }

    public void addGame(Game game) {
        games.add(game);
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

    public Game findGameById(String gameId) {
        return games.stream().filter(game -> game.getGameId().equals(gameId)).findFirst().orElse(null);
    }

    public ConcurrentHashMap<String, Socket> getSocketMapById(String gameId){
        Game game = findGameById(gameId);
        return game.getSocketMap();
    }

    public String generateGameId(){         // 음 뺄까
        return UUID.randomUUID().toString();
    }
}
