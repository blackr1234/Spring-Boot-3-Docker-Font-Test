package code;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

import javax.imageio.ImageIO;

public final class CaptchaUtils {

    private CaptchaUtils() {}

    public static Captcha generateCaptcha(String text) throws Exception {

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(generateCaptchaImage(text), "jpg", os);

            final byte[] bytes = os.toByteArray();
            final String base64 = Base64.getEncoder().encodeToString(bytes);

            final Captcha captcha = new Captcha();
            captcha.setValue(text);
            captcha.setImageBase64(base64);

            return captcha;
        }
    }

    private static BufferedImage generateCaptchaImage(String text) {

        System.setProperty("java.awt.headless", "true");

        final Random random = new SecureRandom();
        final int width = 18 * text.length();
        final int height = 40;
        final double shearFactor = random.nextBoolean() ? -0.7 : 0.7;
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2d = image.createGraphics();
        final AffineTransform transform = new AffineTransform();

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, width, height);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.setColor(Color.BLACK);

        transform.shear(shearFactor, 0);
        g2d.setTransform(transform);

        // Draw the text
        final int x = shearFactor > 0 ? 0 : 25;
        g2d.drawString(text, x, 25);

        // Draw 5 curved noise lines
        for (int i=0; i<5; i++) {
            final CubicCurve2D curve = new CubicCurve2D.Double(
                    random.nextInt(width), random.nextInt(height),
                    random.nextInt(width), random.nextInt(height),
                    random.nextInt(width), random.nextInt(height),
                    random.nextInt(width), random.nextInt(height)
            );
            g2d.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g2d.setStroke(new BasicStroke(1));
            g2d.draw(curve);
        }

        g2d.dispose();

        return image;
    }
}