package common;

import java.io.Serializable;

public class Comment implements Serializable {
    private static int nextCommentId = 0;
    int commentId;
    String writer;
    String content;
    int likes;

    private Comment(int commentId, String writer, String content) {
        this.commentId = commentId;
        this.writer = writer;
        this.content = content;
        this.likes = 0;
    }
    public static synchronized Comment from(String writer, String content) {
        int commentId = getNextCommentId();
        return new Comment(commentId, writer, content);
    }
    // commentId를 자동으로 증가시키는 메서드
    private static synchronized int getNextCommentId() {
        return nextCommentId++;  // 현재 값 반환 후 1 증가
    }

    public static Comment from(int commentId, String writer, String content){
        return new Comment(commentId, writer, content);
    }

    public int getCommentId(){
        return commentId;
    }

    public int addLike(){
        return ++likes;
    }

    public String getWriter() {
        return writer;
    }

    public String getContent() {
        return content;
    }

    public int getLikes() {
        return likes;
    }



}
