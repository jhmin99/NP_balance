package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import common.Game;
import common.User;

public class GameScreen4 extends GameScreen {

	public GameScreen4(ObjectOutputStream out, ObjectInputStream in, String gameScreenTitle, User thisUser,
			HashMap<String, Game> gameList, HashMap<String, User> userList) {
		super(out, in, gameScreenTitle, thisUser, gameList, userList);
	}

}
