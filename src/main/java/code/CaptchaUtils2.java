package code;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CaptchaUtils2 {
    private static final Map<String, BufferedImage> TEXT_IMAGE_CACHE = loadTextImages();

    private static Map<String, BufferedImage> loadTextImages() {

        final Map<String, BufferedImage> images = new LinkedHashMap<>();

        for (int i = 65; i <= 90; i++) {
            final String letter = Character.toString((char) i);
            final String filePath = letter + ".png";
            try (InputStream is = new ClassPathResource("captcha/letter/" + filePath).getInputStream()) {
                images.put(letter, ImageIO.read(is));
            } catch (IOException ex) {
                log.error("Failed to read text image: {}", filePath, ex);
            }
        }

        return images;
    }

    public static BufferedImage generateCaptcha(String captchaText) throws Exception {

        final BufferedImage background = generateNoisyBackground();
        final BufferedImage finalImage = overlayTextOnImage(background, captchaText);

        return finalImage;
    }

    public static BufferedImage generateNoisyBackground() throws Exception {
        int width = 150;
        int height = 60;

        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = image.createGraphics();

        // Fill background with white
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Add pixel noise
        Random rand = new SecureRandom();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = rand.nextInt(128)+128;
                int gr = rand.nextInt(128)+128;
                int b = rand.nextInt(128)+128;
                image.setRGB(x, y, new Color(r, gr, b).getRGB());
            }
        }

        // Optional: Add random lines for extra chaos
        for (int i = 0; i < 20; i++) {
            int x1 = rand.nextInt(width);
            int y1 = rand.nextInt(height);
            int x2 = rand.nextInt(width);
            int y2 = rand.nextInt(height);
            g.setColor(new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
            g.drawLine(x1, y1, x2, y2);
        }

        g.dispose();

        return image;
    }

    public static BufferedImage generateText(String text) {

        final Random rand = new SecureRandom();
        final int width = 40;
        final int height = 40;
        final double shearFactor = rand.nextBoolean() ? -0.7 : 0.7;
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = image.createGraphics();
        final AffineTransform transform = new AffineTransform();

        g.setComposite(AlphaComposite.Clear);
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, width, height);
        g.setComposite(AlphaComposite.SrcOver);
        g.setFont(new Font("Arial", Font.BOLD, 25));
        g.setColor(new Color(rand.nextInt(128), rand.nextInt(128), rand.nextInt(128)));

        transform.shear(shearFactor, 0);
        g.setTransform(transform);

        // Draw the text
        final int x = shearFactor > 0 ? 0 : 25;
        g.drawString(text, x, 25);

        g.dispose();

        return image;
    }

    public static BufferedImage overlayTextOnImage(BufferedImage background, String text) throws IOException {

        Random rand = new SecureRandom();
        Graphics2D g2d = background.createGraphics();

        for (int i=0; i<text.length(); i++) {
            g2d.drawImage(
                TEXT_IMAGE_CACHE.get(text.substring(i, i+1)),
                (rand.nextInt(10)+30)*i,
                rand.nextInt(10)+10,
                null);
        }

        g2d.dispose();

        return background;
    }
}