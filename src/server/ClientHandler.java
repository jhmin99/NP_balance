package server;

import common.Candidate;
import common.Game;
import common.User;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

                    case "REGISTER","LOGIN" -> handleUserAuth(action);  // 성공, 실패 응답
                    case "LOGOUT" -> handleLogout();                    // server.ClientHandler username 초기화 및 응답

                    case "GAME_LIST" -> sendAllGameList();              // Game 리스트 (전체) 응답
                    case "GAME_LIST_VOTED" -> sendGameListVoted();    // Game 리스트 (playedGameIds 필터링) 응답
                    case "GAME_LIST_ID" -> sendGameListById();        // Game 리스트 (createdGameIds 필터링) 응답

                    case "USER_LIST" -> sendAllUserList();               // User 해시맵 (전체) 응답

                    case "ADD_GAME" -> addGame();                       // 성공, 실패 응답

                    case "ENTER_GAME" -> handleClientEnterGame();       // 성공, 실패 응답
                    case "EXIT_GAME" -> handleClientExitGame();         // 성공, 실패 응답

                    case "GAME_DETAILS" -> sendGameDetails();           // GAME 객체 응답

                    case "LIKE" -> handleLike();                        // GAME 객체 브로드캐스트 또는 분리
                    case "VOTE" -> handleVote();                        // GAME 객체 브로드캐스트 또는 분리
                    case "CHAT" -> handleChat();                        // GAME 객체 브로드캐스트 또는 분리
                    
                     case "LIKE_CHAT" -> handleLikeChat();
                    // -> 이것도 만들어서 각 Comment 객체의 likes 를 수정하도록 해줘야 함
                    //                  -> 당연하게도, like 이후에는 위처럼 broadcast 처리가 되어야 한다???

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

        if (result) username = name; // LOGIN 이나 REGISTER 성공시에 server.ClientHandler 에서 username 기억
        System.out.println("LOGIN USER NAME: "+username);

        synchronized (output) {
            output.writeObject(result ? action + "_SUCCESS" : action + "_FAIL");
            output.flush();
        }
    }

    private void handleLogout() throws IOException {
        if (username == null) {
            synchronized (output) {
                output.writeObject("LOGOUT_FAIL");
                output.flush();
            }
            return;
        }
        username = null;

        synchronized (output) {
            output.writeObject("LOGOUT_SUCCESS");
            output.flush();
        }
    }

    private void sendAllGameList() throws IOException {
        ConcurrentHashMap<String, Game> allGameMap = gameManager.getAllGames();
        HashMap<String, Game> allGames = new HashMap<>(allGameMap);

        synchronized (output) {
            output.writeObject("GAME_LIST_SUCCESS");
            output.writeObject(allGames);
            output.flush();
        }
    }

    private void sendGameListVoted() throws IOException {
        // 유저가 아직 로그인을 안 했을 경우
        if (username == null) {
            synchronized (output) {
                output.writeObject("GAME_LIST_VOTED_FAIL");
                output.flush();
            }
            return;
        }

        User currentUser = userManager.findUserByName(username);
        // 현재 유저가 없을 경우
        if (currentUser == null) {
            synchronized (output) {
                output.writeObject("GAME_LIST_VOTED_FAIL");
                output.flush();
            }
            return;
        }

        List<String> playedGameIds = currentUser.getPlayedGameIds();
        ConcurrentHashMap<String, Game> allGames = gameManager.getAllGames();
        ConcurrentHashMap<String, Game> votedGames = new ConcurrentHashMap<>();

        for (String gameId : playedGameIds) {
            if (allGames.containsKey(gameId)) {
                votedGames.put(gameId, allGames.get(gameId));
            }
        }

        synchronized (output) {
            output.writeObject("GAME_LIST_VOTE_SUCCESS");
            output.writeObject(votedGames);
            output.flush();
        }
    }

    private void sendGameListById() throws IOException {
        // 유저가 아직 로그인을 안 했을 경우
        if (username == null) {
            synchronized (output) {
                output.writeObject("GAME_LIST_VOTED_FAIL");
                output.flush();
            }
            return;
        }

        User currentUser = userManager.findUserByName(username);
        // 현재 유저가 없을 경우
        if (currentUser == null) {
            synchronized (output) {
                output.writeObject("GAME_LIST_VOTED_FAIL");
                output.flush();
            }
            return;
        }

        List<String> createdGameIds = currentUser.getCreatedGameIds();
        ConcurrentHashMap<String, Game> allGames = gameManager.getAllGames();
        ConcurrentHashMap<String, Game> createdGames = new ConcurrentHashMap<>();

        for (String gameId : createdGameIds) {
            if (allGames.containsKey(gameId)) {
                createdGames.put(gameId, allGames.get(gameId));
            }
        }

        synchronized (output) {
            output.writeObject("GAME_LIST_ID_SUCCESS");
            output.writeObject(createdGames);
            output.flush();
        }
    }

    private void sendAllUserList() throws IOException {
        ConcurrentHashMap<String, User> allUserMap = userManager.getAllUsers();
        HashMap<String, User> allusers = new HashMap<>(allUserMap);

        synchronized (output) {
            output.writeObject("USER_LIST_SUCCESS");
            output.writeObject(allusers);
            output.flush();
        }
    }

    // 아예 Game 객체를 받을 것 인지 아님 지금 처럼 분할해서 올 것인지?
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
        ConcurrentHashMap<String, Socket> socketMap = gameManager.getSocketMapById(gameId);

        if (socketMap.remove(username) != null) {
            synchronized (output) {
                output.writeObject("EXIT_GAME_SUCCESS");
                output.flush();
            }
            // 방 상태 업데이트
            currentRoomId = "NOT_IN_ROOM";
        }
        else {
            synchronized (output) {
                output.writeObject("EXIT_GAME_FAIL");
                output.flush();
            }
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
            synchronized (output) {
                output.writeObject("GAME_DETAILS_SUCCESS");
                output.writeObject(game);
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
                broadcastGameDetails();         // 이 broadcast 를 Game 객체 전체에 대해 보낼지
                                                // 아니면 3개로 나누어 likes, candidates, chat 을 따로 관리할 지 정해야 함
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
        String gameId = (String) input.readObject();
        String message = (String) input.readObject();

        try{
            gameManager.addComment(gameId, username, message);

            synchronized (output) {
                output.writeObject("CHAT_SUCCESS");
                output.flush();
                broadcastGameDetails();
            }
        }catch (Exception e){
            synchronized (output) {
                output.writeObject("CHAT_FAIL");
                output.flush();
            }
        }
    }

    private void handleLikeChat() throws IOException, ClassNotFoundException {
        String gameId = (String) input.readObject();
        int commentId = (int) input.readObject();

        try {
            gameManager.addLikeComment(gameId, commentId);

            synchronized (output) {
                output.writeObject("LIKE_CHAT_SUCCESS");
                output.flush();
                broadcastGameDetails();
            }
        } catch (Exception e) {
            synchronized (output) {
                output.writeObject("LIKE_CHAT_FAIL");
                output.flush();
            }
        }
    }

    private void broadcastGameDetails() throws IOException {
        Map<String, Socket> socketMap = gameManager.getSocketMapById(currentRoomId);
        Collection<Socket> sockets = socketMap.values();
        ObjectOutputStream sOutput = null;

        for(Socket s : sockets){
            sOutput = gameManager.getOOS(s);
            Game game = gameManager.findGameById(currentRoomId);

            synchronized (sOutput) {
                sOutput.writeObject("BROADCAST_INFO");
                sOutput.writeObject(game);
                sOutput.flush();
            }
        }
    }
}
