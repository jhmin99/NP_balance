import game.Candidate;
import game.Game;
import game.GameManager;
import user.User;
import user.UserManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClientHandler extends Thread {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private UserManager userManager;
    private GameManager gameManager;

    private String username;
    private String currentRoomId;

    public ClientHandler(Socket socket, UserManager userManager, GameManager gameManager) {
        this.socket = socket;
        this.userManager = userManager;
        this.gameManager = gameManager;
        this.currentRoomId = "NOT_IN_ROOM";
    }

    @Override
    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            gameManager.addOOS(socket, output);
            input = new ObjectInputStream(socket.getInputStream());
            while (true) {
                System.out.println("Waiting for action...");
                String action = (String) input.readObject();
                System.out.println("Action received: " + action);

                switch (action) {
                    case "REGISTER","LOGIN" -> handleUserAuth(action);

                    case "GAME_LIST" -> sendAllGameList();
//                    case "GAME_LIST_VOTED" -> sendGameListVoted();
//                    case "GAME_LIST_ID" -> sendGameListById();
//
//                    case "USER_LIST" -> sendUserList();

                    case "ADD_GAME" -> addGame();

                    case "ENTER_GAME" -> handleClientEnterGame();
                    case "EXIT_GAME" -> handleClientExitGame();

                    case "GAME_DETAILS" -> sendGameDetails();   // ENTER_GAME 로직으로 합쳐질 수도

                    case "LIKE" -> handleLike();
                    case "VOTE" -> handleVote();
                    case "CHAT" -> handleChat();

                    default -> {
                        System.out.println("Received action: " + action); // 요청 내용 출력
                        output.writeObject("Invalid command");
                        output.flush();
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleUserAuth(String action) throws IOException, ClassNotFoundException {
        String name = (String) input.readObject();
        String password = (String) input.readObject();
        boolean result = "REGISTER".equals(action) ?
                userManager.registerUser(name, password) :
                userManager.authenticateUser(name, password);

        if (result) username = name; // LOGIN 이나 REGISTER 성공시에 ClientHandler 에서 username 기억
        System.out.println("LOGINED USER NAME: "+username);

        synchronized (output) {
            output.writeObject(result ? action + "_SUCCESS" : action + "_FAIL");
            output.flush();
        }
    }

    private void sendAllGameList() throws IOException {
        List<Game> allGames = gameManager.getAllGames();
        List<Map<String, Object>> gameDataList = allGames.stream().map(game -> {
            Map<String, Object> gameData = new HashMap<>();
            gameData.put("gameId", game.getGameId());
            gameData.put("title", game.getTitle());
            gameData.put("author", game.getAuthor());
            gameData.put("likes", game.getLikes());
            return gameData;
        }).collect(Collectors.toList());

        synchronized (output) {
            output.writeObject("GAME_LIST_SUCCESS");
            output.writeObject(gameDataList);
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
        game.addCandidate(candidate1);
        game.addCandidate(candidate2);
        gameManager.addGame(game);

        User user = userManager.findUserByName(username);
        user.addCreatedGameId(game.getGameId());

        synchronized (output) {
            output.writeObject("ADD_GAME_SUCCESS");
            output.flush();
        }
    }

    private void handleClientEnterGame() throws IOException, ClassNotFoundException {
        String gameId = (String) input.readObject();
        currentRoomId = gameId;
        ConcurrentHashMap<String, Socket> socketMap = gameManager.getSocketMapById(gameId);
        socketMap.put(username, socket);

        synchronized (output) {
            output.writeObject("ENTER_GAME_SUCCESS");
            output.flush();
        }
    }

    private void handleClientExitGame() throws IOException, ClassNotFoundException {
        String gameId = (String) input.readObject();
        currentRoomId = "NOT_IN_ROOM";
        ConcurrentHashMap<String, Socket> socketMap = gameManager.getSocketMapById(gameId);
        socketMap.remove(username);
        synchronized (output) {
            output.writeObject("EXIT_GAME_SUCCESS");
            output.flush();
        }
    }

    private void sendGameDetails() throws IOException, ClassNotFoundException {
        String gameId = (String)input.readObject();
        Game game = gameManager.findGameById(gameId);

        if (game == null){
            output.writeObject("GAME_DETAILS_FAIL");
            output.flush();
        }
        else{
            Map<String, Object> gameDetails = new HashMap<>();
            gameDetails.put("title", game.getTitle());
            gameDetails.put("author", game.getAuthor());

            if (!game.getCandidates().isEmpty()) {
                Candidate candidate1 = game.getCandidates().get(0);
                Candidate candidate2 = game.getCandidates().get(1);

                gameDetails.put("candidate1", candidate1.getName());
                gameDetails.put("candidate2", candidate2.getName());
                gameDetails.put("candidate1Votes", candidate1.getVotes());
                gameDetails.put("candidate2Votes", candidate2.getVotes());
                gameDetails.put("candidate1Image", candidate1.getImageData()); // byte[]
                gameDetails.put("candidate2Image", candidate2.getImageData()); // byte[]
            }

            synchronized (output) {
                output.writeObject("GAME_DETAILS_SUCCESS");
                output.writeObject(gameDetails);
                output.flush();
            }
        }
    }

    private void handleLike() throws IOException, ClassNotFoundException {
        String gameId = (String) input.readObject();
        Game game = gameManager.findGameById(gameId);

        String createdUsername = game.getAuthor();
        User user = userManager.findUserByName(createdUsername);
        user.addLike();

        try{
            gameManager.addLikeToGame(gameId);
            synchronized (output) {
                output.writeObject("LIKE_SUCCESS");
                output.flush();
                broadcastGameDetails();
            }
        }catch (Exception e){
            synchronized (output) {
                output.writeObject("LIKE_FAIL");
                output.flush();
            }
        }
    }

    private void handleVote() throws IOException, ClassNotFoundException {
        String gameId = (String) input.readObject();
        int candidateNumber = (int) input.readObject();
        try{
            gameManager.addVote(gameId, candidateNumber);

            User user = userManager.findUserByName(username);
            user.addPlayedGameId(gameId);

            synchronized (output) {
                output.writeObject("VOTE_SUCCESS");
                output.flush();
                broadcastGameDetails();
            }
        }catch (Exception e){
            synchronized (output) {
                output.writeObject("VOTE_FAIL");
                output.flush();
            }
        }
    }

    private void handleChat() throws IOException, ClassNotFoundException {
        String chatContent = (String) input.readObject();
        synchronized (output) {
            broadcastChat(chatContent);
            output.flush();
        }
    }

    private void broadcastGameDetails() throws IOException {
        Map<String, Socket> socketMap = gameManager.getSocketMapById(currentRoomId);
        Collection<Socket> sockets = socketMap.values();
        ObjectOutputStream sOutput = null;

        for(Socket s : sockets){
            sOutput = gameManager.getOOS(s);
            Game game = gameManager.findGameById(currentRoomId);

            Map<String, Object> gameDetails = new HashMap<>();
            gameDetails.put("title", game.getTitle());
            gameDetails.put("author", game.getAuthor());

            if (!game.getCandidates().isEmpty()) {
                Candidate candidate1 = game.getCandidates().get(0);
                Candidate candidate2 = game.getCandidates().get(1);

                gameDetails.put("candidate1", candidate1.getName());
                gameDetails.put("candidate2", candidate2.getName());
                gameDetails.put("candidate1Votes", candidate1.getVotes());
                gameDetails.put("candidate2Votes", candidate2.getVotes());
                gameDetails.put("candidate1Image", candidate1.getImageData()); // byte[]
                gameDetails.put("candidate2Image", candidate2.getImageData()); // byte[]
            }
            synchronized (sOutput) {
                sOutput.writeObject("BROADCAST_INFO");
                sOutput.writeObject(gameDetails);
                sOutput.flush();
            }
        }
    }

    private void broadcastChat(String line) throws IOException {
        System.out.println("브로드캐스트 메세지: "+line);
        System.out.println("currentRoomId: "+currentRoomId);
        Map<String, Socket> socketMap = gameManager.getSocketMapById(currentRoomId);
        Collection<Socket> sockets = socketMap.values();
        ObjectOutputStream sOutput = null;
        for(Socket s : sockets){
            if(s == socket) continue;
            sOutput = gameManager.getOOS(s);
            if (sOutput != null) {
                synchronized (sOutput) {
                    sOutput.writeObject("BROADCAST_CHAT");
                    sOutput.writeObject(username + ": " + line);
                    sOutput.flush();
                }
            }
        }
    }

    private BufferedImage receiveImage() throws IOException, ClassNotFoundException {
        byte[] imageData = (byte[]) input.readObject();
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        return ImageIO.read(bais);
    }
}
