package client;

import common.Game;
import common.User;

import javax.swing.*;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScreen5 extends GameScreen {

	public GameScreen5(ObjectOutputStream out, ObjectInputStream in, String gameScreenTitle, User thisUser,
					   HashMap<String, Game> gameList, HashMap<String, User> userList) {
		super(out, in, gameScreenTitle, thisUser, gameList, userList);
	}

	@Override
	public void showScreen() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 500);
		frame.setLayout(new BorderLayout());

		JLabel titleLabel = new JLabel("랭킹 화면");
		titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		frame.add(titleLabel, BorderLayout.NORTH);

		JPanel rankingPanel = new JPanel();
		rankingPanel.setLayout(new BoxLayout(rankingPanel, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(rankingPanel);
		frame.add(scrollPane, BorderLayout.CENTER);

		JButton backButton = new JButton("뒤로 가기");
		frame.add(backButton, BorderLayout.SOUTH);

		refreshRankingList(rankingPanel);

		backButton.addActionListener(e -> {
			GameScreen2 gameScreen2 = new GameScreen2(out, in, "게임 목록", _thisUser, _gameList, _userList);
			gameScreen2.showScreen();
			closeScreen();
		});

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void refreshRankingList(JPanel rankingPanel) {
		try {
			synchronized (_lock) {
				out.writeObject("USER_LIST");
				out.flush();

				_userList = (HashMap<String, User>) in.readObject();
				String response = (String) in.readObject();

				if ("USER_LIST_SUCCESS".equals(response)) {
					rankingPanel.removeAll();

					List<Map.Entry<String, User>> sortedUsers = new ArrayList<>(_userList.entrySet());
					sortedUsers.sort((e1, e2) -> Integer.compare(e2.getValue().getTotalLikes(), e1.getValue().getTotalLikes()));

					for (Map.Entry<String, User> entry : sortedUsers) {
						User user = entry.getValue();
						JLabel userLabel = new JLabel(user.getName() + " - 좋아요: " + user.getTotalLikes());
						userLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
						rankingPanel.add(userLabel);
					}

					rankingPanel.revalidate();
					rankingPanel.repaint();
				} else {
					JOptionPane.showMessageDialog(frame, "랭킹 데이터를 불러오는 데 실패했습니다.");
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "서버와의 통신 중 오류 발생");
			e.printStackTrace();
		}
	}
}