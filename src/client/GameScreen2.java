package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import common.Game;
import common.User;


public class GameScreen2 extends GameScreen {

	public GameScreen2(ObjectOutputStream out, ObjectInputStream in, String gameScreenTitle, User thisUser,
			HashMap<String, Game> gameList, HashMap<String, User> userList) {
		super(out, in, gameScreenTitle, thisUser, gameList, userList);
	}

	// override
	public void showScreen() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 500); // 적절한 크기로 설정
		frame.setLayout(new BorderLayout());

		// 상단 패널: 중앙 제목과 최신순, 좋아요 순 버튼 (오른쪽 정렬)
		JPanel topPanel = new JPanel(new BorderLayout());

		JLabel titleLabel = new JLabel("게임 방 선택"); // 투표한 게임, 생성한 게임 변경
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
		topPanel.add(titleLabel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); // 오른쪽 정렬
		JButton renewalGameRoomList = new JButton("새로고침");
		JButton latestOrderButton = new JButton("최신순");
		JButton likesOrderButton = new JButton("좋아요순");
		buttonPanel.add(renewalGameRoomList);
		buttonPanel.add(latestOrderButton);
		buttonPanel.add(likesOrderButton);

		topPanel.add(buttonPanel, BorderLayout.EAST);

		// 중앙 패널: 방 목록 (스크롤 패널) // 여기가 제일 중요
		JPanel roomPanel = showGameList(_gameList);
		JScrollPane scrollPane = new JScrollPane(roomPanel);
		scrollPane.setPreferredSize(new Dimension(450, 300));

		// 오른쪽 패널: 사이드 버튼들
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new GridLayout(5, 1, 10, 10)); // 4개의 버튼 세로 정렬, 간격 10
		sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JButton logoutButton = new JButton("로그아웃");
		JButton createGameButton = new JButton("게임 생성");
		JButton voteGameButton = new JButton("투표한 게임");
		JButton createdGameButton = new JButton("생성한 게임");
		JButton rankingButton = new JButton("랭킹");
		sidePanel.add(logoutButton);
		sidePanel.add(createGameButton);
		sidePanel.add(voteGameButton);
		sidePanel.add(createdGameButton);
		sidePanel.add(rankingButton);

		// 하단 패널: 사용자 정보
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		bottomPanel.setBorder(BorderFactory.createTitledBorder("사용자 정보"));
		JLabel userInfoLabel = new JLabel("이름: " + _thisUser.getName() + " 좋아요: " + _thisUser.getTotalLikes() + " 만든 방: " + _thisUser.getCreatedGameIds().size());
		bottomPanel.add(userInfoLabel);

		// 레이아웃에 추가
		frame.add(topPanel, BorderLayout.NORTH); // 상단
		frame.add(scrollPane, BorderLayout.CENTER); // 중앙
		frame.add(sidePanel, BorderLayout.EAST); // 오른쪽
		frame.add(bottomPanel, BorderLayout.SOUTH); // 하단

		// 화면 중앙에 표시
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		// 이벤트 핸들러
		renewalGameRoomList.addActionListener(null); // 화면 자체를 바꾸는게 맞을지 모르겠네,,,

		latestOrderButton.addActionListener(null);

		likesOrderButton.addActionListener(null);
		
		logoutButton.addActionListener(e -> {
			closeScreen();
		});
		
		createGameButton.addActionListener(null);
		
		voteGameButton.addActionListener(e -> {

			try {

				synchronized (_lock) {

					out.writeObject("GAME_LIST_VOTE");
					out.writeObject(_thisUser);
					out.flush();

					_gameList = (HashMap<String, Game>) in.readObject();
					if (((String) in.readObject()).equals("GAME_LIST_SUCCESS")) {

						GameScreen2 userVoteScreen = new GameScreen2(out, in, _thisUser + "가 투표한 방", _thisUser, _gameList, _userList);
						userVoteScreen.showScreen();
						closeScreen();
					} else {
						JOptionPane.showMessageDialog(frame, "로드 실패");
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		
		createdGameButton.addActionListener(e -> {

			try {

				synchronized (_lock) {

					out.writeObject("GAME_LIST_ID");
					out.writeObject(_thisUser.getName());
					out.flush();

					_gameList = (HashMap<String, Game>) in.readObject();
					if ((_gameList != null) && ((String) in.readObject()).equals("GAME_LIST_ID_SUCCESS")) {

						GameScreen2 userGameScreen = new GameScreen2(out, in, _thisUser.getName() + "가 만든 방", _thisUser, _gameList, _userList);
						userGameScreen.showScreen();
						closeScreen();
					} else {
						JOptionPane.showMessageDialog(frame, "로드 실패");
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});

//		rankingButton.addActionListener(e -> {
//			
//			try {
//				
//				synchronized(_lock) {
//					
//					out.writeObject("USER_LIST");
//					out.flush();
//					
//					_userList = (HashMap<String, User>)in.readObject();
//					if ((_userList != null) && ((String)in.readObject()).equals("USER_LIST_SUCCESS")) {
//						
//						GameScreen5 gs5 = new GameScreen5(_thisUser, _userList, _gameList, socket, in, out, "랭킹 페이지");
//						gs5.showScreen();
//						closeScreen();
//					} else {
//						JOptionPane.showMessageDialog(frame, "로드 실패");
//					}
//						
//				}
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//		});
	}
	
	public JPanel showGameList(HashMap<String, Game> gameList) {
	    JPanel roomPanel = new JPanel();
	    roomPanel.setLayout(new BoxLayout(roomPanel, BoxLayout.Y_AXIS)); // 세로로 정렬
	    roomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 패딩 추가

	    for (Map.Entry<String, Game> entry : gameList.entrySet()) {
	        String gameKey = entry.getKey(); // 키 값 (필요 시 사용 가능)
	        Game g = entry.getValue(); // Game 객체
	        
	        JPanel room = new JPanel();
	        room.setLayout(new BorderLayout());
	        room.setPreferredSize(new Dimension(400, 60));
	        room.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	        room.setBackground(Color.WHITE);

	        // 방 정보를 표시 (필요 시 gameKey 추가 가능)
	        JLabel roomInfo = new JLabel(g.getTitle() + " by " + g.getAuthor());
	        room.add(roomInfo, BorderLayout.CENTER);

	        // 입장 버튼 추가
	        JButton enterButton = new JButton("입장");
	        enterButton.addActionListener(e -> {
	            // 입장 버튼 클릭 시 동작 정의
	            System.out.println("Entering room: " + g.getTitle());
	        });
	        room.add(enterButton, BorderLayout.EAST);

	        // 방을 패널에 추가
	        roomPanel.add(room);
	        roomPanel.add(Box.createVerticalStrut(5)); // 방 사이 간격 추가
	    }

	    return roomPanel;
	}

}
