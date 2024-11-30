package client;

import common.Game;
import common.User;

import javax.swing.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public abstract class GameScreen {
	protected Object _lock = new Object();
	protected ObjectOutputStream out;
	protected ObjectInputStream in;
	protected JFrame frame;
	protected User _thisUser;
	protected HashMap<String, Game> _gameList;
	protected HashMap<String, User> _userList;

	public GameScreen(ObjectOutputStream out, ObjectInputStream in, String gameScreenTitle, User thisUser,
					  HashMap<String, Game> gameList, HashMap<String, User> userList) {
		this.out = out;
		this.in = in;
		this._thisUser = thisUser;
		this._gameList = gameList;
		this._userList = userList;
		this.frame = new JFrame(gameScreenTitle);
	}

	public abstract void showScreen();

	public void closeScreen() {
		frame.dispose();
	}
}