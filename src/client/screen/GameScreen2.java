package client.screen;

import common.Game;
import common.User;

import javax.swing.*;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GameScreen2 extends GameScreen {

	private JPanel gameListPanel;  // 게임 목록 패널을 전역으로 선언
	private JScrollPane scrollPane;  // 스크롤 패널을 전역으로 선언

	private boolean calledFromScreen5 = false;	// Screen5에서 호출된 경우 리스트 재요청을 막기 위한 변수

	public GameScreen2(ObjectOutputStream out, ObjectInputStream in, String gameScreenTitle, User thisUser,
					   HashMap<String, Game> gameList, HashMap<String, User> userList) {
		super(out, in, gameScreenTitle, thisUser, gameList, userList);
		System.out.println("생성한게임:"
				+ thisUser.getCreatedGameIds());
		System.out.println("참여한게임:"
				+ thisUser.getPlayedGameIds());
	}

	@Override
	public void showScreen() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 500);
		frame.setLayout(new BorderLayout());

		// 타이틀 및 상단 버튼 패널
		JPanel topPanel = new JPanel(new BorderLayout());
		JLabel titleLabel = new JLabel("게임 방 선택");
		titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		topPanel.add(titleLabel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton refreshButton = new JButton("새로고침");
		JButton latestOrderButton = new JButton("최신순");
		JButton likesOrderButton = new JButton("좋아요순");
		buttonPanel.add(refreshButton);
		buttonPanel.add(latestOrderButton);
		buttonPanel.add(likesOrderButton);
		topPanel.add(buttonPanel, BorderLayout.EAST);

		// 중앙 게임 목록 패널
		gameListPanel = new JPanel();
		gameListPanel.setLayout(new BoxLayout(gameListPanel, BoxLayout.Y_AXIS));
		gameListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		scrollPane = new JScrollPane(gameListPanel);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		// 오른쪽 사이드 버튼 패널
		JPanel sidePanel = new JPanel(new GridLayout(6, 1, 10, 10));
		JButton logoutButton = new JButton("로그아웃");
		JButton createGameButton = new JButton("게임 생성");
		JButton allGameButton = new JButton("전체 게임");
		JButton voteGameButton = new JButton("투표한 게임");
		JButton createdGameButton = new JButton("생성한 게임");
		JButton rankingButton = new JButton("랭킹");
		sidePanel.add(logoutButton);
		sidePanel.add(createGameButton);
		sidePanel.add(allGameButton);
		sidePanel.add(voteGameButton);
		sidePanel.add(createdGameButton);
		sidePanel.add(rankingButton);

		// 하단 사용자 정보 패널
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottomPanel.setBorder(BorderFactory.createTitledBorder("사용자 정보"));
		JLabel userInfoLabel = new JLabel(
				"이름: " + _thisUser.getName() + " | 좋아요: " + _thisUser.getTotalLikes() + " | 만든 방: " + _thisUser.getCreatedGameIds().size());
		bottomPanel.add(userInfoLabel);

		// 프레임에 추가
		frame.add(topPanel, BorderLayout.NORTH);
		frame.add(scrollPane, BorderLayout.CENTER);
		frame.add(sidePanel, BorderLayout.EAST);
		frame.add(bottomPanel, BorderLayout.SOUTH);

		// 중앙에 표시
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		// **초기 게임 목록 가져오기**
		fetchGameList(gameListPanel, scrollPane);

		// 이벤트 처리
		refreshButton.addActionListener(e -> fetchGameList(gameListPanel, scrollPane));
		latestOrderButton.addActionListener(e -> sortGameListByLatest(gameListPanel, scrollPane));
		likesOrderButton.addActionListener(e -> sortGameListByLikes(gameListPanel, scrollPane));
		logoutButton.addActionListener(e -> logout());
		createGameButton.addActionListener(e -> showGameCreationScreen());
		voteGameButton.addActionListener(e -> showVotedGamesScreen());
		createdGameButton.addActionListener(e -> showCreatedGamesScreenByName(_thisUser.getName()));
		rankingButton.addActionListener(e -> showRankingScreen());
		allGameButton.addActionListener(e -> fetchGameList(gameListPanel, scrollPane));
	}

	private void fetchGameList(JPanel gameListPanel, JScrollPane scrollPane) {
		if(calledFromScreen5) {
			updateGameListUI(gameListPanel, scrollPane);
			setCalledFromScreen5(false);
			return;
		}
		try {
			frame.setTitle("게임 목록");
			synchronized (_lock) {
				out.writeObject("GAME_LIST");
				out.flush();

				String response = (String) in.readObject();
				Object receivedData = in.readObject();

				if ("GAME_LIST_SUCCESS".equals(response) && receivedData instanceof Map) {
					_gameList = new HashMap<>((Map<String, Game>) receivedData);
					updateGameListUI(gameListPanel, scrollPane);  // 게임 리스트 업데이트
				} else if ("GAME_LIST_FAIL".equals(response)) {
					JOptionPane.showMessageDialog(frame, "게임 목록을 불러오는 데 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "서버와의 통신 중 오류 발생", "오류", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void updateGameListUI(JPanel gameListPanel, JScrollPane scrollPane) {
		gameListPanel.removeAll();

		if (_gameList.isEmpty()) {
			JLabel noGameLabel = new JLabel("게임방이 없습니다. 생성해주세요!");
			noGameLabel.setHorizontalAlignment(SwingConstants.CENTER);
			noGameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
			gameListPanel.add(noGameLabel);
		} else {
			for (Map.Entry<String, Game> entry : _gameList.entrySet()) {
				Game game = entry.getValue();

				JPanel gamePanel = new JPanel(new BorderLayout());
				gamePanel.setPreferredSize(new Dimension(580, 50));  // 게임 패널 크기 일정
				gamePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

				JLabel gameInfoLabel = new JLabel(game.getTitle() + " by " + game.getAuthor() + " | 좋아요: " + game.getLikes());
				gameInfoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
				gamePanel.add(gameInfoLabel, BorderLayout.CENTER);

				JButton enterButton = new JButton("입장");
				enterButton.addActionListener(e -> enterGameRoom(game));
				gamePanel.add(enterButton, BorderLayout.EAST);

				gameListPanel.add(gamePanel);
				gameListPanel.add(Box.createVerticalStrut(5));  // 간격 추가
			}
		}

		gameListPanel.revalidate();
		gameListPanel.repaint();
		scrollPane.revalidate();
		scrollPane.repaint();
	}

	private void sortGameListByLatest(JPanel gameListPanel, JScrollPane scrollPane) {
		List<Map.Entry<String, Game>> sortedList = _gameList.entrySet().stream()
				.sorted((e1, e2) -> e2.getValue().getCreatedTime().compareTo(e1.getValue().getCreatedTime()))
				.collect(Collectors.toList());

		_gameList = sortedList.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
				(oldValue, newValue) -> oldValue, LinkedHashMap::new));
		updateGameListUI(gameListPanel, scrollPane);
	}

	private void sortGameListByLikes(JPanel gameListPanel, JScrollPane scrollPane) {
		List<Map.Entry<String, Game>> sortedList = _gameList.entrySet().stream()
				.sorted((e1, e2) -> Integer.compare(e2.getValue().getLikes(), e1.getValue().getLikes()))
				.collect(Collectors.toList());

		_gameList = sortedList.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
				(oldValue, newValue) -> oldValue, LinkedHashMap::new));
		updateGameListUI(gameListPanel, scrollPane);
	}

	private void enterGameRoom(Game game) {
		try {
			synchronized (_lock) {
				out.writeObject("ENTER_GAME");
				out.writeObject(game.getGameId());
				out.flush();

				String response = (String) in.readObject();
				if ("ENTER_GAME_SUCCESS".equals(response)) {
					_thisUser.setCurrentGameId(game.getGameId());
					new GameScreen4(out, in, "게임 방", _thisUser, _gameList, _userList).showScreen();
					closeScreen();
				} else if ("ENTER_GAME_FAIL".equals(response)) {
					JOptionPane.showMessageDialog(frame, "게임 방 입장 실패: 게임이 존재하지 않거나 다른 오류가 발생했습니다.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "게임 방 입장 중 오류 발생");
		}
	}

	private void showGameCreationScreen() {
		new GameScreen3(out, in, "게임 생성", _thisUser, _gameList, _userList).showScreen();
		closeScreen();
	}

	private void showVotedGamesScreen() {
		try {
			synchronized (_lock) {
				out.writeObject("GAME_LIST_VOTED");  // '투표한 게임' 목록 요청
				out.flush();

				String response = (String) in.readObject();

				if ("GAME_LIST_VOTE_SUCCESS".equals(response)) {
					Object receivedData = in.readObject();  // 클라이언트에서 받은 데이터

					System.out.println("receivedData: " + receivedData);

					_gameList = new HashMap<>((Map<String, Game>) receivedData);

					if (_gameList.isEmpty()) {
						JLabel noGameLabel = new JLabel("투표한 게임이 없습니다.");
						noGameLabel.setHorizontalAlignment(SwingConstants.CENTER);
						noGameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
						gameListPanel.removeAll();
						gameListPanel.add(noGameLabel);
						gameListPanel.revalidate();
						gameListPanel.repaint();
					} else {
						updateGameListUI(gameListPanel, scrollPane);  // UI 갱신
					}
				} else if ("GAME_LIST_VOTED_FAIL".equals(response)) {
					JOptionPane.showMessageDialog(frame, "투표한 게임 목록 로드 실패", "오류", JOptionPane.ERROR_MESSAGE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "투표한 게임 목록 로드 중 오류 발생");
		}
	}


	private void showCreatedGamesScreenByName(String username) {
		try {
			synchronized (_lock) {
				out.writeObject("GAME_LIST_ID");  // '생성한 게임' 목록 요청
				out.writeObject(username);  // '생성한 게임' 목록 요청
				out.flush();

				String response = (String) in.readObject();
				Object receivedData = in.readObject();  // 클라이언트에서 받은 데이터

				if ("GAME_LIST_ID_SUCCESS".equals(response) && receivedData instanceof Map) {
					_gameList = new HashMap<>((Map<String, Game>) receivedData);

					if (_gameList.isEmpty()) {
						JLabel noGameLabel = new JLabel("생성한 게임이 없습니다.");
						noGameLabel.setHorizontalAlignment(SwingConstants.CENTER);
						noGameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
						gameListPanel.removeAll();
						gameListPanel.add(noGameLabel);
						gameListPanel.revalidate();
						gameListPanel.repaint();
					} else {
						updateGameListUI(gameListPanel, scrollPane);  // UI 갱신
					}
				} else if ("GAME_LIST_ID_FAIL".equals(response)) {
					JOptionPane.showMessageDialog(frame, "생성한 게임 목록 로드 실패", "오류", JOptionPane.ERROR_MESSAGE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "생성한 게임 목록 로드 중 오류 발생");
		}
	}

	private void showRankingScreen() {
		try {
			synchronized (_lock) {
				out.writeObject("USER_LIST");
				out.flush();

				String response = (String) in.readObject();
				Object receivedData = in.readObject();

				if ("USER_LIST_SUCCESS".equals(response) && receivedData instanceof Map) {
					_userList = new HashMap<>((Map<String, User>) receivedData);
					new GameScreen5(out, in, "랭킹", _thisUser, _gameList, _userList).showScreen();
					closeScreen();
				} else if ("USER_LIST_FAIL".equals(response)) {
					JOptionPane.showMessageDialog(frame, "랭킹 데이터 로드 실패", "오류", JOptionPane.ERROR_MESSAGE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "랭킹 데이터 로드 중 오류 발생");
		}
	}

	private void logout() {
		closeScreen();
	}

	public void setCalledFromScreen5(boolean bool){
		calledFromScreen5 = bool;
	}
}