package server;

import common.Candidate;
import common.Game;
import common.User;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final ObjectOutputStream output;
    private final ObjectInputStream input;
    private final UserManager userManager;
    private final GameManager gameManager;

    private String username;
    private String currentRoomId;

    public ClientHandler(Socket socket, UserManager userManager, GameManager gameManager) throws IOException {
        this.socket = socket;
        this.userManager = userManager;
        this.gameManager = gameManager;
        this.output = new ObjectOutputStream(socket.getOutputStream());
        this.input = new ObjectInputStream(socket.getInputStream());

        // 소켓과 출력 스트림 매핑
        gameManager.addOOS(socket, output);
    }

    @Override
    public void run() {
        try {
            while (true) {
                String action = (String) input.readObject();
                System.out.println("Action received: " + action);

                switch (action) {
                    case "REGISTER", "LOGIN" -> handleUserAuth(action);
                    case "LOGOUT" -> handleLogout();
                    case "GAME_LIST" -> sendAllGameList();
                    case "GAME_LIST_VOTED" -> sendGameListVoted();
                    case "GAME_LIST_ID" -> sendGameListById();
                    case "USER_LIST" -> sendAllUserList();
                    case "ADD_GAME" -> addGame();
                    case "ENTER_GAME" -> handleClientEnterGame();
                    case "EXIT_GAME" -> handleClientExitGame();
                    case "GAME_DETAILS" -> sendGameDetails();
                    case "LIKE" -> handleLike();
                    case "VOTE" -> handleVote();
                    case "CHAT" -> handleChat();
                    case "LIKE_CHAT" -> handleLikeChat();
                    default -> handleInvalidCommand(action);
                }
            }
        } catch (EOFException e) {
            System.err.println("Client disconnected.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void handleUserAuth(String action) throws IOException, ClassNotFoundException {
        String name = (String) input.readObject();
        String password = (String) input.readObject();

        boolean result = "REGISTER".equals(action) ?
                userManager.registerUser(name, password) :
                userManager.authenticateUser(name, password);

        synchronized (output) {
            output.writeObject(result ? action + "_SUCCESS" : action + "_FAIL");
            output.flush();

            if (result && "LOGIN".equals(action)) {
                username = name;
                User authenticatedUser = userManager.findUserByName(name);
                output.writeObject(authenticatedUser);
                output.flush();
            }
        }
}

    private void handleLogout() throws IOException {
        if (username == null) {
            synchronized (output) {
                sendResponse("LOGOUT_FAIL");
                output.flush();
            }
            return;
        }
        username = null;

        synchronized (output) {
            sendResponse("LOGOUT_SUCCESS");
            output.flush();
        }
    }
    private void sendAllGameList() throws IOException {
        synchronized (output) {
            sendResponse("GAME_LIST_SUCCESS");
            output.writeObject(gameManager.getAllGames());
            output.flush();
        }
    }

    private void sendGameListVoted() throws IOException {
        if (username == null) {
            synchronized (output) {
                sendResponse("GAME_LIST_VOTED_FAIL");
            }
            return;
        }

        User currentUser = userManager.findUserByName(username);
        if (currentUser == null) {
            synchronized (output) {
                sendResponse("GAME_LIST_VOTED_FAIL");
            }
            return;
        }

        synchronized (output) {
            sendResponse("GAME_LIST_VOTE_SUCCESS");
            output.writeObject(filterGamesByVoted());
            output.flush();
        }
    }

    private void sendGameListById() throws IOException, ClassNotFoundException {
        String targetName = (String) input.readObject();

        if (targetName == null) {
            synchronized (output) {
                sendResponse("GAME_LIST_ID_FAIL");
            }
            return;
        }

        User currentUser = userManager.findUserByName(targetName);
        if (currentUser == null) {
            synchronized (output) {
                sendResponse("GAME_LIST_ID_FAIL");
            }
            return;
        }

        synchronized (output) {
            sendResponse("GAME_LIST_ID_SUCCESS");
            output.writeObject(filterGamesById(currentUser.getName()));
            output.flush();
        }
    }

    private void sendAllUserList() throws IOException {
        synchronized (output) {  // 동기화 추가
            sendResponse("USER_LIST_SUCCESS");
            HashMap<String, User> userHashMap = new HashMap<>(userManager.getAllUsers());
            output.writeObject(userHashMap);
            output.flush();
        }
    }

    private void addGame() throws IOException, ClassNotFoundException {
        String title = (String) input.readObject();
        String author = (String) input.readObject();
        String candidate1Name = (String) input.readObject();
        String candidate2Name = (String) input.readObject();

        byte[] candidate1Data = (byte[]) input.readObject();
        byte[] candidate2Data = (byte[]) input.readObject();

        BufferedImage candidate1Image = ImageIO.read(new ByteArrayInputStream(candidate1Data));
        BufferedImage candidate2Image = ImageIO.read(new ByteArrayInputStream(candidate2Data));

        Candidate candidate1 = new Candidate(candidate1Name, candidate1Image);
        Candidate candidate2 = new Candidate(candidate2Name, candidate2Image);

        Game game = new Game(gameManager.generateGameId(), author, title);
        game.setCandidate1(candidate1);
        game.setCandidate2(candidate2);

        gameManager.addGame(game);

        User user = userManager.findUserByName(username);
        user.addCreatedGameId(game.getGameId());
        userManager.saveUsers();
        userManager.reloadUsers();

        synchronized (output) {
            sendResponse("ADD_GAME_SUCCESS");
            output.writeObject(game.getGameId());
            output.flush();
        }
    }

    private void handleClientEnterGame() throws IOException, ClassNotFoundException {
        String gameId = (String) input.readObject();
        if (gameId == null || gameId.isEmpty()) {
            // gameId가 유효한지 체크
            sendResponse("ENTER_GAME_FAIL");
            return;
        }
        currentRoomId = gameId;  // currentRoomId를 gameId로 초기화

        // 게임 ID가 유효한지 확인
        Game game = gameManager.findGameById(gameId);
        if (game == null) {
            sendResponse("ENTER_GAME_FAIL");
            return;
        }
        gameManager.addSocketToGame(gameId, username, socket);
        User user = userManager.findUserByName(username);
        user.setCurrentGameId(gameId);
        userManager.saveUsers();
        userManager.reloadUsers();

        synchronized (output) {
            sendResponse("ENTER_GAME_SUCCESS");
        }
    }

    private void handleClientExitGame() throws IOException {
        gameManager.removeSocketFromGame(currentRoomId, username);
        currentRoomId = null;

        synchronized (output) {
            sendResponse("EXIT_GAME_SUCCESS");
        }
    }

    private void sendGameDetails() throws IOException {
        Game game = gameManager.findGameById(currentRoomId);
        if (game == null) {
            synchronized (output) {
                sendResponse("GAME_DETAILS_FAIL");
            }
        } else {
            synchronized (output) {
                sendResponse("GAME_DETAILS_SUCCESS");
                output.writeObject(game);
                output.flush();
            }
        }
    }

    private void handleLike() throws IOException, ClassNotFoundException {
        String gameId = (String) input.readObject();
        Game game = gameManager.findGameById(gameId);

        if (game == null) {
            synchronized (output) {
                sendResponse("LIKE_FAIL");
            }
            return; // 게임이 없으면 종료
        }

        gameManager.addLikeToGame(gameId);
        User user = userManager.findUserByName(gameManager.findGameById(gameId).getAuthor());
        user.setTotalLikes(user.getTotalLikes() + 1);
        userManager.saveUsers();
        userManager.reloadUsers();

        synchronized (output) {
            sendResponse("LIKE_SUCCESS");
        }
        broadcastGameDetails(gameId);
    }

    private void handleVote() throws IOException, ClassNotFoundException {
        String gameId = (String) input.readObject();
        int candidateNumber = (int) input.readObject();
        Game game = gameManager.findGameById(gameId);

        if (game == null || candidateNumber < 1 || candidateNumber > 2) {  // 유효하지 않은 후보 번호
            synchronized (output) {
                sendResponse("VOTE_FAIL");
            }
            return; // 유효하지 않으면 종료
        }
        gameManager.addVote(gameId, candidateNumber);
        User user = userManager.findUserByName(username);

        user.addPlayedGameId(gameId);
        userManager.saveUsers();

        synchronized (output) {
            sendResponse("VOTE_SUCCESS");
        }
        broadcastGameDetails(gameId);
    }

    private void handleChat() throws IOException, ClassNotFoundException {
        String gameId = (String) input.readObject();
        String message = (String) input.readObject();
        Game game = gameManager.findGameById(gameId);

        if (game == null || message == null || message.trim().isEmpty()) {  // 유효하지 않은 게임 또는 비어 있는 메시지
            synchronized (output) {
                sendResponse("CHAT_FAIL");
            }
            return; // 유효하지 않으면 종료
        }
        gameManager.addComment(gameId, username, message);

        synchronized (output) {
            sendResponse("CHAT_SUCCESS");
        }
        broadcastGameDetails(gameId);
    }

    private void handleLikeChat() throws IOException, ClassNotFoundException {
        String gameId = (String) input.readObject();
        int commentId = (int) input.readObject();
        Game game = gameManager.findGameById(gameId);

        if (game == null || commentId < 0) {  // 유효하지 않은 게임 또는 댓글 번호
            synchronized (output) {
                sendResponse("LIKE_CHAT_FAIL");
                System.out.println(game);
                System.out.println(commentId);
            }
            return; // 유효하지 않으면 종료
        }
        gameManager.addLikeComment(gameId, commentId);

        synchronized (output) {
            sendResponse("LIKE_CHAT_SUCCESS");
        }
        broadcastGameDetails(gameId);
    }

    private void handleInvalidCommand(String action) throws IOException {
        synchronized (output) {
            sendResponse("Invalid command: " + action);
        }
    }

    private void broadcastGameDetails(String gameId) throws IOException {
        gameManager.reloadGames();
        ConcurrentHashMap<String, Socket> socketMap = gameManager.getSocketMapById(gameId);

        if (socketMap == null || socketMap.isEmpty()) {
            System.err.println("No clients connected to game ID: " + gameId);
            return;
        }

        Game game = gameManager.findGameById(gameId);
        if (game == null) {
            System.err.println("Game not found for broadcasting: " + gameId);
            return;
        }

        for (Socket s : socketMap.values()) {
            ObjectOutputStream sOutput = gameManager.getOOS(s);
            synchronized (sOutput) {
                sOutput.writeObject("BROADCAST_INFO");
                sOutput.writeObject(game);
                sOutput.flush();
            }
        }
    }

    private void sendResponse(String message) throws IOException {
        synchronized (output) {  // 동기화 추가
            output.writeObject(message);
            output.flush();
        }
    }

    private void cleanup() {
        if (currentRoomId != null && username != null) {
            gameManager.removeSocketFromGame(currentRoomId, username);
        }
        System.out.println("Cleanup completed for client: " + username);
    }

    private HashMap<String, Game> filterGamesById(String targetName) {
        User user = userManager.findUserByName(targetName);
        HashMap<String, Game> filteredGames = new HashMap<>();
        if (user != null) {
            for (String gameId : user.getCreatedGameIds()) {
                Game game = gameManager.findGameById(gameId);
                if (game != null) {
                    filteredGames.put(gameId, game);
                }
            }
        }
        return filteredGames;
    }

    private HashMap<String, Game> filterGamesByVoted() {
        User user = userManager.findUserByName(username);
        HashMap<String, Game> filteredGames = new HashMap<>();
        if (user != null) {
            for (String gameId : user.getPlayedGameIds()) {
                Game game = gameManager.findGameById(gameId);
                if (game != null) {
                    filteredGames.put(gameId, game);
                }
            }
        }
        return filteredGames;
    }
}