package client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import common.Game;
import common.User;

public class GameScreen1 extends GameScreen {

	public GameScreen1(ObjectOutputStream out, ObjectInputStream in, String gameScreenTitle, User thisUser,
			HashMap<String, Game> gameList, HashMap<String, User> userList) {
		super(out, in, gameScreenTitle, thisUser, gameList, userList);
	}

	// override
	public void showScreen() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 500);
		frame.setLayout(new BorderLayout(10, 10));

		// Create the main panel and set layout
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		// Add title label
		JLabel titleLabel = new JLabel("실시간 밸런스 게임");
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
		mainPanel.add(titleLabel);

		// Add spacing
		mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		// Add ID input field
		JLabel idLabel = new JLabel("아이디 입력");
		idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		idLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
		JTextField idField = new JTextField(20);
		idField.setMaximumSize(new Dimension(300, 40));

		mainPanel.add(idLabel);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		mainPanel.add(idField);

		// Add spacing
		mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		// Add password input field
		JLabel passwordLabel = new JLabel("비밀번호 입력");
		passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		passwordLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
		JPasswordField passwordField = new JPasswordField(20);
		passwordField.setMaximumSize(new Dimension(300, 40));

		mainPanel.add(passwordLabel);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		mainPanel.add(passwordField);

		// Add spacing
		mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

		// Add button panel for "유저 등록" and "게임 입장"
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));

		JButton registerButton = new JButton("유저 등록");
		JButton enterGameButton = new JButton("게임 입장");

		registerButton.setPreferredSize(new Dimension(120, 40));
		enterGameButton.setPreferredSize(new Dimension(120, 40));

		buttonPanel.add(registerButton);
		buttonPanel.add(enterGameButton);

		mainPanel.add(buttonPanel);

		// Center the main panel in the frame
		JPanel wrapperPanel = new JPanel(new GridBagLayout());
		wrapperPanel.add(mainPanel);

		frame.add(wrapperPanel);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		registerButton.addActionListener(e -> {
			
			String id = idField.getText();
			String password = new String(passwordField.getPassword());
			
			_thisUser.setName(id);
			_thisUser.setPassword(password);
			
			try {
				
				synchronized (_lock) {
					out.writeObject("REGISTER");
					out.reset();
					out.writeObject(_thisUser);
					out.flush();
					
					if (((String)in.readObject()).equals("REGISTER_SUCCESS")) {
						JOptionPane.showMessageDialog(frame, "등록 성공");
					} else {
						JOptionPane.showMessageDialog(frame, "등록 실패");
					}	
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		
		enterGameButton.addActionListener(e -> {
			
			String id = idField.getText();
			String password = new String(passwordField.getPassword());
			
			try {
				
				synchronized (_lock) {
					
					out.writeObject("LOGIN");
					out.writeObject(id);
					out.writeObject(password);
					out.flush();
					
					String response = (String)in.readObject();
					if (response.equals("LOGIN_SUCCESS")) {
						JOptionPane.showMessageDialog(frame, "로그인 성공");
						out.writeObject("GAME_LIST");
						out.flush();
						_gameList = (HashMap<String, Game>)in.readObject();
						if ((_gameList != null) && ((String)in.readObject()).equals("GAME_LIST_SUCCESS")) {
							GameScreen gs2 = new GameScreen2(out, in, "전체 게임 방", _thisUser, _gameList, _userList);
							gs2.showScreen();
							closeScreen();
						} else {
							JOptionPane.showMessageDialog(frame, "로드 실패");
						}
					} else {
						JOptionPane.showMessageDialog(frame, "로그인 실패");
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
	}
}
