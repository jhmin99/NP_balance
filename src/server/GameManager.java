package server;

import common.Comment;
import common.Game;
import common.User;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    private static final String GAME_FILE = "Game.dat";

    private ConcurrentHashMap<String, Game> games;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Socket>> socketMaps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Socket, ObjectOutputStream> outputStreamMap = new ConcurrentHashMap<>();

    public GameManager() {
        this.games = loadGames();
        if (this.games == null) {
            this.games = new ConcurrentHashMap<>();
        }
    }

    public void reloadGames() {
        this.games = loadGames();
        System.out.println("Games reloaded from file.");
    }

    public ConcurrentHashMap<String, Game> getAllGames() {
        return games;
    }

    public HashMap<String, Game> filterGamesByVoted(User user) {
        HashMap<String, Game> filteredGames = new HashMap<>();
        if (user != null) {
            for (String gameId : user.getPlayedGameIds()) {
                Game game = findGameById(gameId);
                if (game != null) {
                    filteredGames.put(gameId, game);
                }
            }
        }
        return filteredGames;
    }

    public HashMap<String, Game> filterGamesById(User user) {
        HashMap<String, Game> filteredGames = new HashMap<>();
        if (user != null) {
            for (String gameId : user.getCreatedGameIds()) {
                Game game = findGameById(gameId);
                if (game != null) {
                    filteredGames.put(gameId, game);
                }
            }
        }
        return filteredGames;
    }

    public Game findGameById(String gameId) {
        return games.get(gameId);
    }

    public void addGame(Game game) {
        games.put(game.getGameId(), game);
        saveGames();
        reloadGames();
    }

    public void addComment(String gameId, String writer, String message) {
        Game game = findGameById(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found with ID: " + gameId);
        }
        game.addComment(writer, message);
        saveGames();
        reloadGames();
    }

    public void addLikeToGame(String gameId) {
        Game game = findGameById(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found with ID: " + gameId);
        }
        game.like();
        saveGames();
        reloadGames();
    }

    public void addVote(String gameId, int candidateNumber) {
        Game game = findGameById(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found with ID: " + gameId);
        }
        game.addVote(candidateNumber);
        saveGames();
        reloadGames();
    }

    public void addLikeComment(String gameId, int commentId) {
        Game game = findGameById(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found with ID: " + gameId);
        }
        Comment comment = game.getCommentById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found with ID: " + commentId);
        }
        comment.addLike();
        saveGames();
        reloadGames();
    }

    public void addSocketToGame(String gameId, String username, Socket socket) {
        socketMaps.putIfAbsent(gameId, new ConcurrentHashMap<>());
        ConcurrentHashMap<String, Socket> socketMap = socketMaps.get(gameId);
        socketMap.put(username, socket);
        System.out.println("Added socket for user " + username + " in game " + gameId);
    }

    public void removeSocketFromGame(String gameId, String username) {
        ConcurrentHashMap<String, Socket> socketMap = socketMaps.get(gameId);
        if (socketMap != null) {
            socketMap.remove(username);
            System.out.println("Removed socket for user " + username + " in game " + gameId);
        }
    }

    public ConcurrentHashMap<String, Socket> getSocketMapById(String gameId) {
        return socketMaps.get(gameId);
    }

    public ObjectOutputStream getOOS(Socket socket) {
        return outputStreamMap.get(socket);
    }

    public void addOOS(Socket socket, ObjectOutputStream oos) {
        outputStreamMap.put(socket, oos);
    }

    private ConcurrentHashMap<String, Game> loadGames() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(GAME_FILE))) {
            return (ConcurrentHashMap<String, Game>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Game file not found or empty. Starting fresh.");
            return null;
        }
    }
    public String generateGameId(){
        return UUID.randomUUID().toString();
    }
    private void saveGames() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(GAME_FILE))) {
            oos.writeObject(games);
            System.out.println("Games saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}