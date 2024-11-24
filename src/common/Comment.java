package common;

import java.io.Serializable;

public class Comment implements Serializable {
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

    public static Comment from(int commentId, String writer, String content){
        return new Comment(commentId, writer, content);
    }

    public int getCommentId(){
        return commentId;
    }

    public int addLike(){
        return ++likes;
    }
}
