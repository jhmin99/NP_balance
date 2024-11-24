package common;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class Candidate implements Serializable {
    private String name;
    private transient BufferedImage image; // 직렬화되지 않도록 transient 지정
    private int votes;
    private byte[] imageData; // 이미지 데이터를 저장하는 바이트 배열

    public Candidate(String name, BufferedImage image) {
        this.name = name;
        this.image = image;
        this.votes = 0;
        this.imageData = bufferedImageToByteArray(image);
    }

    public String getName() {
        return name;
    }

    public BufferedImage getImage() {
        if (image == null && imageData != null) {
            image = byteArrayToBufferedImage(imageData);
        }
        return image;
    }

    public int getVotes() {
        return votes;
    }

    public void addVote() {
        votes++;
    }

    public void subVote() {
        votes--;
    }

    private byte[] bufferedImageToByteArray(BufferedImage img) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private BufferedImage byteArrayToBufferedImage(byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            return ImageIO.read(bais);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getImageData() {
        return this.imageData;
    }
}
