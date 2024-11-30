package client;

import common.Game;
import common.User;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class ClientMain {
	
	private static final String SERVER_ADDRESS = "localhost";
	private static final int PORT = 8081;
	
	private static Socket socket;
	private static ObjectInputStream in;
	private static ObjectOutputStream out;

	public static void main(String[] args) {
		
		try {
			socket = new Socket(SERVER_ADDRESS, PORT);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		// DummyFile
		HashMap<String, Game> dummyGList = new HashMap<>();
		for (int i = 0; i < 5; i++) {
			dummyGList.put("" + i, new Game("" + i, "title" + i, "author" + i));
		}
		
		User user = new User();
		GameScreen1 gs1 = new GameScreen1(out, in, "실시간 밸런스 게임", user, null, null);
		gs1.showScreen();
		
		// 테스트 중
//		GameScreen2 gs2 = new GameScreen2(out, in, "room2", user, dummyGList, null);
//		gs2.showScreen();
	}

}
