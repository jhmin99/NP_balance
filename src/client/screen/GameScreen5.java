package client.screen;

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

//RankingPage
public class GameScreen5 extends GameScreen {

	public GameScreen5(ObjectOutputStream out, ObjectInputStream in, String gameScreenTitle, User thisUser,
					   HashMap<String, Game> gameList, HashMap<String, User> userList) {
		super(out, in, gameScreenTitle, thisUser, gameList, userList);
	}

	@Override
	public void showScreen() {
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setSize(800, 500);
//		frame.setLayout(new BorderLayout());
//
//		JLabel titleLabel = new JLabel("랭킹 화면");
//		titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
//		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
//		frame.add(titleLabel, BorderLayout.NORTH);
//
//		JPanel rankingPanel = new JPanel();
//		rankingPanel.setLayout(new BoxLayout(rankingPanel, BoxLayout.Y_AXIS));
//		JScrollPane scrollPane = new JScrollPane(rankingPanel);
//		frame.add(scrollPane, BorderLayout.CENTER);
//
//		JButton backButton = new JButton("뒤로 가기");
//		frame.add(backButton, BorderLayout.SOUTH);
//
//		refreshRankingList(rankingPanel);
//
//		backButton.addActionListener(e -> {
//			GameScreen2 gameScreen2 = new GameScreen2(out, in, "게임 목록", _thisUser, _gameList, _userList);
//			gameScreen2.showScreen();
//			closeScreen();
//		});
//
//		frame.setLocationRelativeTo(null);
//		frame.setVisible(true);

		// gyu
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 500);
		frame.setLayout(new BorderLayout(10, 10));

		//
		JPanel titlePanel = new JPanel();
		JLabel titleLabel = new JLabel("랭킹 순위", SwingConstants.CENTER);
		titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
		titlePanel.add(titleLabel);
		frame.add(titleLabel, BorderLayout.NORTH);

		// 이것도 만들어야함
		JPanel roomPanel = showUserList(_userList);

		JScrollPane scrollPane = new JScrollPane(roomPanel);
		scrollPane.setPreferredSize(new Dimension(450, 300));
		frame.add(scrollPane, BorderLayout.CENTER); // 중앙

		// 뒤로 가기 버튼 (하단)
		JPanel bottomPanel = new JPanel(new BorderLayout());
		JButton backButton = new JButton("뒤로 가기");
		bottomPanel.add(backButton, BorderLayout.WEST);
		frame.add(bottomPanel, BorderLayout.SOUTH);

		// 프레임 표시
		frame.setVisible(true);

		backButton.addActionListener(e -> handleBackButton());
	}

	private void refreshRankingList(JPanel rankingPanel) {
		try {
			synchronized (_lock) {
				out.writeObject("USER_LIST");
				out.flush();

				String response = (String) in.readObject();
				_userList = (HashMap<String, User>) in.readObject();

				if ("USER_LIST_SUCCESS".equals(response)) {
					rankingPanel.removeAll();

					List<Map.Entry<String, User>> sortedUsers = new ArrayList<>(_userList.entrySet());
					sortedUsers.sort(
							(e1, e2) -> Integer.compare(e2.getValue().getTotalLikes(), e1.getValue().getTotalLikes()));

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

	private JPanel showUserList(HashMap<String, User> userList) {

		JPanel roomPanel = new JPanel();
		roomPanel.setLayout(new BoxLayout(roomPanel, BoxLayout.Y_AXIS)); // 세로로 정렬
		roomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		List<Map.Entry<String, User>> entries = new ArrayList<>(userList.entrySet());
		entries.sort((entry1, entry2) -> Integer.compare(entry2.getValue().getTotalLikes(),
				entry1.getValue().getTotalLikes()));

		for (Map.Entry<String, User> entry : entries) {
			String userId = entry.getKey();
			User userInfo = entry.getValue();

			JPanel room = new JPanel();
			room.setLayout(new BorderLayout());
			room.setPreferredSize(new Dimension(400, 60));
			room.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			room.setBackground(Color.WHITE);

			JLabel roomInfo = new JLabel(userId + ", " + userInfo.getTotalLikes());
			room.add(roomInfo, BorderLayout.CENTER);

			JButton enterButton = new JButton("게임보기");
			room.add(enterButton, BorderLayout.EAST);

			roomPanel.add(room);
			roomPanel.add(Box.createVerticalStrut(5)); // 방 사이 간격 추가

			enterButton.addActionListener(e -> handleSelUserGame(userId));
		}

		return roomPanel;
	}

	private void handleSelUserGame(String id) {
		try {
			synchronized (_lock) {
				out.writeObject("GAME_LIST_ID");
				out.writeObject(id);
				out.flush();

				String response = (String)in.readObject();
				if (response.equals("GAME_LIST_ID_SUCCESS")) {
					HashMap<String, Game> gameList = (HashMap<String, Game>)in.readObject();
					GameScreen2 gs2 = new GameScreen2(out, in, id + "가 만든 방", _thisUser, gameList, _userList);
					gs2.setCalledFromScreen5(true);
					gs2.showScreen();
					closeScreen();
				} else if (response.equals("GAME_LIST_ID_FAIL")) {
					JOptionPane.showMessageDialog(frame, "게임 리스트를 불러오는데 실패하였습니다.");
				} else {
					JOptionPane.showMessageDialog(frame, "알 수 없는 오류가 발생했습니다.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleBackButton() {
		try {
			synchronized (_lock) {
				out.writeObject("GAME_LIST_ID");
				out.writeObject(_thisUser.getName());
				out.flush();

				String response = (String)in.readObject();
				if (response.equals("GAME_LIST_ID_SUCCESS")) {
					HashMap<String, Game> gameList = (HashMap<String, Game>) in.readObject();
					GameScreen2 gs2 = new GameScreen2(out, in, _thisUser.getName() + "가 만든 방", _thisUser, _gameList, _userList);
					gs2.showScreen();
					closeScreen();

				} else if (response.equals("GAME_LIST_ID_FAIL")) {
					JOptionPane.showMessageDialog(frame, "게임 리스트를 불러오는데 실패하였습니다.");
				} else {
					JOptionPane.showMessageDialog(frame, "알 수 없는 오류가 발생했습니다.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}