package client.screen;

import common.Candidate;
import common.Comment;
import common.Game;
import common.User;

import javax.swing.*;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

//GamePlayPage
public class GameScreen4 extends GameScreen {

	private JPanel bestCommentPanel; // 베스트 댓글 패널
	private JPanel normalCommentPanel; // 일반 댓글 패널
	private JTextField commentInput; // 댓글 입력 필드
	private JLabel totalVotesLabel; // 총 투표 수 라벨
	private Game currentGame; // 현재 게임 상태
	private JLabel likeCountLabel;
	private final ReentrantLock lock = new ReentrantLock(); // 데이터 동기화 락
	private JPanel bestCommentSection;

	private volatile boolean isStopped = false;

	public GameScreen4(ObjectOutputStream out, ObjectInputStream in, String gameScreenTitle, User thisUser,
					   HashMap<String, Game> gameList, HashMap<String, User> userList) {
		super(out, in, gameScreenTitle, thisUser, gameList, userList);
		currentGame = gameList.get(thisUser.getCurrentGameId());
		System.out.println("생성한게임:"
				+ thisUser.getCreatedGameIds());
		System.out.println("참여한게임:"
				+ thisUser.getPlayedGameIds());
	}
	@Override
	public void showScreen() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 800); // 적절한 화면 크기
		frame.setLayout(new BorderLayout());

		// 최상단 '뒤로 가기' 버튼 패널
		JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); // 좌측 정렬 및 간격 조정
		JButton backButton = new JButton("뒤로 가기");
		backButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
		backButton.setPreferredSize(new Dimension(100, 40));
		backButton.addActionListener(e -> handleExitGame());
		backPanel.add(backButton);

		// 상단 게임 정보 및 후보자 영역
		JPanel topPanel = new JPanel(new BorderLayout());

		// 게임 정보 패널
		JPanel gameInfoPanel = new JPanel(new GridLayout(4, 1, 5, 5));  // 4개 항목을 세로로 배치
		JLabel titleLabel = new JLabel("게임: " + currentGame.getTitle());
		titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// 작성자, 총 투표 수, 추천 수
		JLabel authorLabel = new JLabel("작성자: " + currentGame.getAuthor());
		authorLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
		authorLabel.setHorizontalAlignment(SwingConstants.CENTER);

		totalVotesLabel = new JLabel("총 투표 수: " + currentGame.getVotesNum());
		totalVotesLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
		totalVotesLabel.setHorizontalAlignment(SwingConstants.CENTER);

		likeCountLabel = new JLabel("추천 수: " + currentGame.getLikes());  // 클래스 변수로 설정
		likeCountLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
		likeCountLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// 각 정보 항목을 gameInfoPanel에 추가
		gameInfoPanel.add(titleLabel);
		gameInfoPanel.add(authorLabel);
		gameInfoPanel.add(totalVotesLabel);
		gameInfoPanel.add(likeCountLabel);

		// 후보자 정보 패널
		JPanel candidatePanel = new JPanel(new GridLayout(1, 2, 20, 20));
		candidatePanel.add(createCandidatePanel(currentGame.getCandidate1(), 1));
		candidatePanel.add(createCandidatePanel(currentGame.getCandidate2(), 2));

		// gameInfoPanel을 상단에 추가
		topPanel.add(gameInfoPanel, BorderLayout.NORTH);
		topPanel.add(candidatePanel, BorderLayout.CENTER);

		// 오른쪽 상단에 '추천하기' 버튼 추가
		JPanel likePanel = new JPanel();
		likePanel.setLayout(new BoxLayout(likePanel, BoxLayout.X_AXIS));

		likePanel.setPreferredSize(new Dimension(200, 30));
		JButton likeButton = new JButton("추천하기");
		likeButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
		likeButton.setPreferredSize(new Dimension(120, 40));
		likeButton.addActionListener(e -> handleLike());  // 추천 처리 메서드

		likePanel.add(likeButton);


		JPanel midPanel = new JPanel(new BorderLayout());
		midPanel.add(likePanel, BorderLayout.EAST);
		topPanel.add(midPanel, BorderLayout.SOUTH);

		// 댓글 섹션
		JPanel commentSection = new JPanel(new BorderLayout());
		JLabel commentLabel = new JLabel("댓글");
		commentLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
		commentLabel.setHorizontalAlignment(SwingConstants.CENTER);

		bestCommentSection = new JPanel();
		bestCommentSection.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		bestCommentSection.setPreferredSize(new Dimension(900, 180));

		JLabel bestCommentTitle = new JLabel("베스트 댓글");
		bestCommentTitle.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		bestCommentSection.add(bestCommentTitle);

		normalCommentPanel = new JPanel();
		normalCommentPanel.setLayout(new BoxLayout(normalCommentPanel, BoxLayout.Y_AXIS));
		JScrollPane normalScrollPane = new JScrollPane(normalCommentPanel);
		normalScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		commentInput = new JTextField();
		JButton submitCommentButton = new JButton("댓글 작성");
		submitCommentButton.addActionListener(e -> handleAddComment());

		JPanel inputPanel = new JPanel(new BorderLayout());
		inputPanel.add(commentInput, BorderLayout.CENTER);
		inputPanel.add(submitCommentButton, BorderLayout.EAST);

		commentSection.add(bestCommentSection, BorderLayout.NORTH);
		commentSection.add(normalScrollPane, BorderLayout.CENTER);
		commentSection.add(inputPanel, BorderLayout.SOUTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, commentSection);
		splitPane.setDividerLocation(400);
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerSize(5);

		frame.add(backPanel, BorderLayout.NORTH);
		frame.add(splitPane, BorderLayout.CENTER);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		listenForBroadcasts();
		loadComments();
	}

	private void handleExitGame() {
		try {
			synchronized (_lock) {
				out.writeObject("EXIT_GAME");
				out.flush();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "뒤로 가기 처리 중 오류 발생", "오류", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void handleLike() {
		try {
			synchronized (_lock) {
				out.writeObject("LIKE");
				out.writeObject(currentGame.getGameId());
				out.flush();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "추천 처리 중 오류 발생");
			e.printStackTrace();
		}
	}

	private void updateLikeLabel() {
		SwingUtilities.invokeLater(() -> {
			likeCountLabel.setText("추천 수: " + currentGame.getLikes()); // 추천 수로 업데이트
		});
	}

	private JPanel createCandidatePanel(Candidate candidate, int candidateNumber) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JLabel imageLabel = new JLabel(new ImageIcon(candidate.getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH)));
		imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel nameLabel = new JLabel(candidate.getName());
		nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel likeLabel = new JLabel("좋아요: " + candidate.getVotes());
		likeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		// 투표 버튼
		JButton voteButton = new JButton();
		voteButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		System.out.println(_thisUser.getCreatedGameIds());

		// 사용자가 이미 이 게임에 투표했는지 확인
		if (isUserVoted()) {
			voteButton.setText("투표 완료");
			voteButton.setEnabled(false); // 버튼 비활성화
		} else {
			voteButton.setText("투표");
			voteButton.addActionListener(e -> handleVote(candidateNumber, voteButton));
		}

		panel.add(imageLabel);
		panel.add(Box.createVerticalStrut(10)); // 간격
		panel.add(nameLabel);
		panel.add(likeLabel);
		panel.add(Box.createVerticalStrut(10)); // 간격
		panel.add(voteButton);

		return panel;
	}

	private boolean isUserVoted() {
		System.out.println(_thisUser.getPlayedGameIds());
		System.out.println(_thisUser.getPlayedGameIds().contains(currentGame.getGameId()));
		return _thisUser.getPlayedGameIds().contains(currentGame.getGameId());
	}

	private void updateGame(Game updatedGame) {
		SwingUtilities.invokeLater(() -> {
			try {
				synchronized (_lock) {
					System.out.println("Updating game UI for game: " + updatedGame.getGameId());

					if (updatedGame == null) return;

					this.currentGame = updatedGame;

					totalVotesLabel.setText("총 투표 수: " + currentGame.getVotesNum());

//					JPanel candidatePanel = (JPanel) ((JPanel) ((JSplitPane) frame.getContentPane().getComponent(0)).getTopComponent()).getComponent(1);

					JSplitPane splitPane = (JSplitPane) frame.getContentPane().getComponent(1); // splitPane 가져오기
					JPanel topPanel = (JPanel) splitPane.getTopComponent(); // topPanel 가져오기
					JPanel candidatePanel = (JPanel) topPanel.getComponent(1); // 후보자 패널 가져오기

					candidatePanel.removeAll();
					candidatePanel.add(createCandidatePanel(currentGame.getCandidate1(), 1));
					candidatePanel.add(createCandidatePanel(currentGame.getCandidate2(), 2));

					candidatePanel.revalidate();
					candidatePanel.repaint();

					loadComments();
					System.out.println("UI updated successfully for game: " + currentGame.getGameId());
				}
			} catch (Exception e) {
				System.err.println("Error during UI update: " + e.getMessage());
				e.printStackTrace();
			}
		});
	}

	private void handleAddComment() {
		String content = commentInput.getText().trim();
		if (content.isEmpty()) {
			JOptionPane.showMessageDialog(frame, "댓글 내용을 입력하세요!");
			return;
		}
		try {
			synchronized (_lock) {
				out.writeObject("CHAT");
				out.writeObject(currentGame.getGameId());
				out.writeObject(content);
				out.flush();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "댓글 작성 중 오류 발생");
			e.printStackTrace();
		}
	}

	private void handleVote(int candidateNumber, JButton voteButton) {
		try {
			synchronized (_lock) {
				voteButton.setEnabled(false);
				voteButton.setText("투표 완료");
				out.writeObject("VOTE");
				out.writeObject(currentGame.getGameId());
				out.writeObject(candidateNumber);
				out.flush();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "투표 중 오류 발생");
			e.printStackTrace();
		}
	}


	private void loadComments() {
		SwingUtilities.invokeLater(() -> {
			try {
				synchronized (_lock) {
					bestCommentSection.removeAll(); // 베스트 댓글 영역 초기화
					normalCommentPanel.removeAll(); // 일반 댓글 영역 초기화

					List<Comment> comments = currentGame.getComments();
					if (comments == null || comments.isEmpty()) return;

					// 베스트 댓글 정렬 및 상위 3개 가져오기
					List<Comment> bestComments = comments.stream()
							.sorted((c1, c2) -> Integer.compare(c2.getLikes(), c1.getLikes()))
							.limit(3)
							.toList();

					// 베스트 댓글 추가
					JLabel bestCommentTitle = new JLabel("베스트 댓글");
					bestCommentTitle.setFont(new Font("맑은 고딕", Font.BOLD, 14));
					bestCommentSection.add(bestCommentTitle);

					for (Comment comment : bestComments) {
						bestCommentSection.add(createCommentPanel(comment, true));
					}

					// 일반 댓글 추가
					for (Comment comment : comments) {
						if (!bestComments.contains(comment)) {
							normalCommentPanel.add(createCommentPanel(comment, false));
						}
					}

					// 레이아웃 새로고침
					bestCommentSection.revalidate();
					bestCommentSection.repaint();
					normalCommentPanel.revalidate();
					normalCommentPanel.repaint();
				}
			} catch (Exception e) {
				e.printStackTrace();
            }
        });
	}

	private JPanel createCommentPanel(Comment comment, boolean isBest) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS)); // 가로로 일렬 배치
		panel.setPreferredSize(new Dimension(980, 50)); // 고정된 크기 설정
		panel.setMaximumSize(new Dimension(980, 50));   // 최대 크기 설정
		panel.setBackground(isBest ? Color.decode("#f0f8ff") : Color.WHITE);

		// 댓글 내용
		JLabel messageLabel = new JLabel(comment.getContent());
		messageLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		messageLabel.setPreferredSize(new Dimension(400, 50)); // 가로 400, 세로 50 고정
		messageLabel.setVerticalAlignment(SwingConstants.CENTER); // 세로 가운데 정렬
		messageLabel.setHorizontalAlignment(SwingConstants.LEFT); // 가로 왼쪽 정렬

		// 좋아요 버튼
		JButton likeButton = new JButton("좋아요 (" + comment.getLikes() + ")");
		likeButton.setPreferredSize(new Dimension(120, 30)); // 버튼 크기 설정
		likeButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		likeButton.addActionListener(e -> handleLikeComment(comment.getCommentId()));

		// 작성자 정보
		JLabel writerLabel = new JLabel("작성자: " + comment.getWriter());
		writerLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		writerLabel.setPreferredSize(new Dimension(150, 50)); // 작성자 정보 크기 고정
		writerLabel.setHorizontalAlignment(SwingConstants.CENTER);
		writerLabel.setVerticalAlignment(SwingConstants.CENTER);

		// 여백 추가를 위한 빈 공간
		Component spacer = Box.createHorizontalGlue();

		// 컴포넌트를 순서대로 추가
		panel.add(Box.createRigidArea(new Dimension(10, 0))); // 좌측 여백
		panel.add(messageLabel);
		panel.add(spacer); // 빈 공간으로 균형 유지
		panel.add(likeButton);
		panel.add(Box.createRigidArea(new Dimension(10, 0))); // 오른쪽 여백
		panel.add(writerLabel);

		return panel;
	}

	private void handleLikeComment(int commentId) {
		try {
			synchronized (_lock) {
				out.writeObject("LIKE_CHAT");
				out.writeObject(currentGame.getGameId());
				out.writeObject(commentId);
				out.flush();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "좋아요 처리 중 오류 발생");
			e.printStackTrace();
		}
	}

	private void listenForBroadcasts() {
		new Thread(() -> {
			try {
				while (!isStopped) {
					String messageType = (String) in.readObject(); // 데이터 유형 태그 읽기
					if ("BROADCAST_INFO".equals(messageType)) {
						// 브로드캐스트 처리
						Game updatedGame = (Game) in.readObject();
						updateGame(updatedGame);
					} else if("EXIT_GAME_SUCCESS".equals(messageType)){
						_thisUser.setCurrentGameId(null);
						isStopped = true;
						SwingUtilities.invokeLater(() -> {
							new GameScreen2(out, in, "Game List", _thisUser, _gameList, _userList).showScreen();
							frame.dispose();
						});
					} else {
						// 그 외는 명령 응답 처리
						String response = messageType; // 메시지 타입 자체가 응답 메시지
						processResponse(response);
					}
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(frame, "브로드캐스트 수신 중 오류 발생");
				e.printStackTrace();
			}
		}).start();
	}


	private void processResponse(String response) {
		SwingUtilities.invokeLater(() -> {
			switch (response) {
				case "CHAT_SUCCESS":
					JOptionPane.showMessageDialog(frame, "댓글 작성 성공!");
					commentInput.setText("");
					loadComments();
					break;
				case "LIKE_CHAT_SUCCESS":
					JOptionPane.showMessageDialog(frame, "댓글 좋아요 성공!");
					commentInput.setText("");
					break;
				case "VOTE_SUCCESS":
					JOptionPane.showMessageDialog(frame, "투표 성공!");
					_thisUser.addPlayedGameId(currentGame.getGameId());
					loadComments();
					break;
				case "LIKE_SUCCESS":
					JOptionPane.showMessageDialog(frame, "좋아요 성공!");
					// 게임 추천 수 증가 후 UI 업데이트
					currentGame.like();  // 게임의 추천 수 증가
					updateLikeLabel();  // 추천 수 레이블 업데이트
					loadComments();
					break;
				case "CHAT_FAIL":
					JOptionPane.showMessageDialog(frame, "댓글 작성 실패. 다시 시도해 주세요.");
					break;
				case "LIKE_CHAT_FAIL":
					JOptionPane.showMessageDialog(frame, "댓글 좋아요 실패. 다시 시도해 주세요.");
					break;
				case "VOTE_FAIL":
					JOptionPane.showMessageDialog(frame, "투표 실패. 이미 투표했거나 오류가 발생했습니다.");
					break;
				case "LIKE_FAIL":
					JOptionPane.showMessageDialog(frame, "추천 실패. 오류가 발생했습니다.");
					break;
				case "EXIT_GAME_FAIL":
					JOptionPane.showMessageDialog(frame, "뒤로 가기 실패", "오류", JOptionPane.ERROR_MESSAGE);
					break;
				default:
					JOptionPane.showMessageDialog(frame, "알 수 없는 응답: " + response);
			}
		});
	}
}