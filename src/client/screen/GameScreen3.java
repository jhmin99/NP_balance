package client.screen;

import common.Game;
import common.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;

//CreateGamePage
public class GameScreen3 extends GameScreen {

	private File candidate1ImageFile = null;
	private File candidate2ImageFile = null;
	private JLabel candidate1ImagePreview;
	private JLabel candidate2ImagePreview;

	public GameScreen3(ObjectOutputStream out, ObjectInputStream in, String gameScreenTitle, User thisUser,
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
		frame.setSize(900, 700);
		frame.setLayout(new BorderLayout());

		// 제목 레이블
		JLabel titleLabel = new JLabel("게임 생성");
		titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 32));
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// 메인 입력 패널
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(20, 20));

		// 후보자 패널
		JPanel candidatesPanel = new JPanel(new GridLayout(1, 2, 20, 0)); // 좌우 대칭

		// 후보자 1 패널
		JPanel candidate1Panel = new JPanel();
		candidate1Panel.setLayout(new BorderLayout(10, 10));
		candidate1Panel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.GRAY), "후보자 1", TitledBorder.LEFT, TitledBorder.TOP,
				new Font("맑은 고딕", Font.BOLD, 14), Color.BLACK));

		JTextField candidate1NameField = new JTextField();
		candidate1NameField.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
		candidate1NameField.setPreferredSize(new Dimension(200, 30));
		JLabel nameLabel1 = new JLabel("이름:");
		JPanel candidate1NamePanel = new JPanel(new BorderLayout());
		candidate1NamePanel.add(nameLabel1, BorderLayout.WEST);
		candidate1NamePanel.add(candidate1NameField, BorderLayout.CENTER);

		candidate1ImagePreview = new JLabel("미리보기 없음", SwingConstants.CENTER);
		candidate1ImagePreview.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		candidate1ImagePreview.setOpaque(true);
		candidate1ImagePreview.setBackground(Color.LIGHT_GRAY);
		candidate1ImagePreview.setPreferredSize(new Dimension(200, 200));

		JButton candidate1ImageButton = new JButton("이미지 업로드");

		candidate1Panel.add(candidate1NamePanel, BorderLayout.NORTH);
		candidate1Panel.add(candidate1ImagePreview, BorderLayout.CENTER);
		candidate1Panel.add(candidate1ImageButton, BorderLayout.SOUTH);

		// 후보자 2 패널
		JPanel candidate2Panel = new JPanel();
		candidate2Panel.setLayout(new BorderLayout(10, 10));
		candidate2Panel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.GRAY), "후보자 2", TitledBorder.LEFT, TitledBorder.TOP,
				new Font("맑은 고딕", Font.BOLD, 14), Color.BLACK));

		JTextField candidate2NameField = new JTextField();
		candidate2NameField.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
		candidate2NameField.setPreferredSize(new Dimension(200, 30));
		JLabel nameLabel2 = new JLabel("이름:");
		JPanel candidate2NamePanel = new JPanel(new BorderLayout());
		candidate2NamePanel.add(nameLabel2, BorderLayout.WEST);
		candidate2NamePanel.add(candidate2NameField, BorderLayout.CENTER);

		candidate2ImagePreview = new JLabel("미리보기 없음", SwingConstants.CENTER);
		candidate2ImagePreview.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		candidate2ImagePreview.setOpaque(true);
		candidate2ImagePreview.setBackground(Color.LIGHT_GRAY);
		candidate2ImagePreview.setPreferredSize(new Dimension(200, 200));

		JButton candidate2ImageButton = new JButton("이미지 업로드");

		candidate2Panel.add(candidate2NamePanel, BorderLayout.NORTH);
		candidate2Panel.add(candidate2ImagePreview, BorderLayout.CENTER);
		candidate2Panel.add(candidate2ImageButton, BorderLayout.SOUTH);

		candidatesPanel.add(candidate1Panel);
		candidatesPanel.add(candidate2Panel);

		// 제목 입력 필드
		JPanel titlePanel = new JPanel(new BorderLayout());
		JLabel gameTitleLabel = new JLabel("게임 제목:");
		gameTitleLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
		JTextField gameTitleField = new JTextField();
		titlePanel.add(gameTitleLabel, BorderLayout.WEST);
		titlePanel.add(gameTitleField, BorderLayout.CENTER);

		mainPanel.add(titlePanel, BorderLayout.NORTH);
		mainPanel.add(candidatesPanel, BorderLayout.CENTER);

		// 생성 버튼
		JButton createButton = new JButton("게임 생성");
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(createButton);

		// 이미지 업로드 동작
		candidate1ImageButton.addActionListener(e -> uploadImage(candidate1ImagePreview, 1));
		candidate2ImageButton.addActionListener(e -> uploadImage(candidate2ImagePreview, 2));

		// 게임 생성 버튼 동작
		createButton.addActionListener(e -> createGame(gameTitleField, candidate1NameField, candidate2NameField));

		frame.add(titleLabel, BorderLayout.NORTH);
		frame.add(mainPanel, BorderLayout.CENTER);
		frame.add(buttonPanel, BorderLayout.SOUTH);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void uploadImage(JLabel previewLabel, int candidateNumber) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("이미지 파일 (JPG, JPEG, PNG)", "jpg", "jpeg", "png"));
		int result = fileChooser.showOpenDialog(frame);

		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			try {
				BufferedImage image = ImageIO.read(selectedFile);

				// 이미지가 성공적으로 로드되면 미리보기 업데이트
				previewLabel.setIcon(new ImageIcon(image.getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
				previewLabel.setText(""); // "미리보기 없음" 텍스트 제거
				previewLabel.setBackground(Color.WHITE); // 배경색을 흰색으로 설정
				previewLabel.setOpaque(true); // 배경색이 보이도록 설정

				// 선택된 이미지 파일 저장
				if (candidateNumber == 1) {
					candidate1ImageFile = selectedFile;
				} else {
					candidate2ImageFile = selectedFile;
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame, "이미지를 불러오는 데 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void createGame(JTextField gameTitleField, JTextField candidate1NameField, JTextField candidate2NameField) {
		String gameTitle = gameTitleField.getText();
		String candidate1Name = candidate1NameField.getText();
		String candidate2Name = candidate2NameField.getText();

		if (gameTitle.isEmpty() || candidate1Name.isEmpty() || candidate2Name.isEmpty() || candidate1ImageFile == null || candidate2ImageFile == null) {
			JOptionPane.showMessageDialog(frame, "모든 필드와 이미지를 입력해야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			byte[] candidate1ImageData = loadImage(candidate1ImageFile);
			byte[] candidate2ImageData = loadImage(candidate2ImageFile);

			synchronized (_lock) {
				out.writeObject("ADD_GAME");
				out.writeObject(gameTitle);
				out.writeObject(_thisUser.getName());
				out.writeObject(candidate1Name);
				out.writeObject(candidate2Name);
				out.writeObject(candidate1ImageData);
				out.writeObject(candidate2ImageData);
				out.flush();

				String response = (String) in.readObject();
				if ("ADD_GAME_SUCCESS".equals(response)) {
					String gameId = (String) in.readObject();
					_thisUser.addCreatedGameId(gameId);
					JOptionPane.showMessageDialog(frame, "게임 생성 성공!");
					GameScreen2 gameScreen2 = new GameScreen2(out, in, "게임 목록", _thisUser, _gameList, _userList);
					gameScreen2.showScreen();
					closeScreen();
				} else {
					JOptionPane.showMessageDialog(frame, "게임 생성 실패");
				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(frame, "서버와의 통신 중 오류 발생", "오류", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}

	private byte[] loadImage(File imageFile) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			BufferedImage image = ImageIO.read(imageFile);
			ImageIO.write(image, "png", baos);
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "이미지 파일을 로드하는 데 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

}