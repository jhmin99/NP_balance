package client;

import client.screen.GameScreen1;
import common.Game;
import common.User;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class ClientMain {
	private static final String SERVER_ADDRESS = "localhost";
	private static final int PORT = 8080;
	private static Socket socket;
	private static ObjectOutputStream out;
	private static ObjectInputStream in;

	public static void main(String[] args) {
		try {
			socket = new Socket(SERVER_ADDRESS, PORT);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(socket.getInputStream());

			User user = new User();
			HashMap<String, Game> gameList = new HashMap<>();
			HashMap<String, User> userList = new HashMap<>();
			GameScreen1 gameScreen1 = new GameScreen1(out, in, "실시간 밸런스 게임", user, gameList, userList);
			gameScreen1.showScreen();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("서버 연결 실패. 클라이언트를 종료합니다.");
		}
	}

	// 소켓 및 스트림 닫기
	public static void closeConnection() {
		try {
			if (in != null) in.close();
			if (out != null) out.close();
			if (socket != null) socket.close();
			System.out.println("클라이언트 연결이 종료되었습니다.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}