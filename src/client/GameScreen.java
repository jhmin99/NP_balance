package client;

import common.Game;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import javax.swing.JFrame;

import common.User;

public class GameScreen {
	
	protected Object _lock = new Object();
	
//	protected Socket socket; // - 필요없는 것 같음
	protected ObjectOutputStream out;
	protected ObjectInputStream in;
	
	protected JFrame frame;
	
	protected User _thisUser;
	protected HashMap<String, Game> _gameList; 
	protected HashMap<String, User> _userList;
	
	// 기본 생성자 필요 시 사용
//	public GameScreen() {}
	
	public GameScreen(
			ObjectOutputStream out,
			ObjectInputStream in,
			String gameScreenTitle,
			User thisUser, 
			HashMap<String, Game> gameList, 
			HashMap<String, User> userList) {
		
		this.out = out;
		this.in = in;
		
		this.frame = new JFrame(gameScreenTitle);
		
		_thisUser = thisUser;
		_gameList = gameList;
		_userList = userList;
	}
	
	public void showScreen() {
		
	}
	
	public void closeScreen() {
		this.frame.dispose();
	}
}
