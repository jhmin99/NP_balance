package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import common.Game;
import common.User;

public class GameScreen5 extends GameScreen {

	public GameScreen5(ObjectOutputStream out, ObjectInputStream in, String gameScreenTitle, User thisUser,
			HashMap<String, Game> gameList, HashMap<String, User> userList) {
		super(out, in, gameScreenTitle, thisUser, gameList, userList);
	}
	
	public void showScreen() {
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
	}
	
//	public JPanel showUserList(HashMap<String, UserInfo> userList) {
//		
//		JPanel roomPanel = new JPanel();
//		
//		roomPanel.setLayout(new BoxLayout(roomPanel, BoxLayout.Y_AXIS)); // 세로로 정렬
//        roomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 패딩 추가
//
//        // 정렬
//        List<Map.Entry<String, UserInfo>> entries = new ArrayList<>(userList.entrySet());
//        entries.sort((entry1, entry2) -> Integer.compare(entry2.getValue().get_totalLike(), entry1.getValue().get_totalLike()));
//        LinkedHashMap<String, UserInfo> sortedMap = new LinkedHashMap<>();
//        for (Map.Entry<String, UserInfo> entry : entries) {
//        	
//        	sortedMap.put(entry.getKey(), entry.getValue());
//        }
//        
//        for (String id : sortedMap.keySet()) {
//        	
//        	MyJPanel room = new MyJPanel(userList.get(id).get_userId(), _lock);
//        	room.initializePanel(id, userList, frame, _thisUser,in, out, socket, this);
//            
//            roomPanel.add(room);
//            roomPanel.add(Box.createVerticalStrut(5)); // 방 사이 간격 추가
//        }
//		
//        return roomPanel;
//	}
	
	public JPanel showUserList(HashMap<String, User> userList) {

	    JPanel roomPanel = new JPanel();

	    roomPanel.setLayout(new BoxLayout(roomPanel, BoxLayout.Y_AXIS)); // 세로로 정렬
	    roomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 패딩 추가

	    // 정렬: 좋아요 순으로 정렬 (내림차순)
	    List<Map.Entry<String, User>> entries = new ArrayList<>(userList.entrySet());
	    entries.sort((entry1, entry2) -> Integer.compare(entry2.getValue().getTotalLikes(), entry1.getValue().getTotalLikes()));

	    // 정렬된 유저 리스트를 기반으로 버튼 생성
	    for (Map.Entry<String, User> entry : entries) {
	        String userId = entry.getKey();
	        User userInfo = entry.getValue();

	        // 버튼 생성
	        JButton userButton = new JButton(userId + " - 좋아요: " + userInfo.getTotalLikes());
	        userButton.setAlignmentX(JButton.CENTER_ALIGNMENT); // 버튼 가운데 정렬
	        userButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

	        // 버튼에 대한 ActionListener 추가
	        userButton.addActionListener(e -> {
	            // 유저 아이디로 접근 해당 -> 해당 유저의 게임리스트 요청
	            System.out.println(userId + " 버튼이 클릭되었습니다!");
	            // 예시: 사용자 정보 출력
	            System.out.println("[User Info] ID: " + userId + ", Likes: " + userInfo.getTotalLikes());
	        });

	        // 패널에 버튼 추가
	        roomPanel.add(userButton);
	        roomPanel.add(Box.createVerticalStrut(5)); // 버튼 사이 간격 추가
	    }

	    return roomPanel;
	}
}
