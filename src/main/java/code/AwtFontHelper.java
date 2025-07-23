package code;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AwtFontHelper {

    public static Font loadFont(String classpathFontFile) {

        System.setProperty("java.awt.headless", "true");

        try (InputStream is = new ClassPathResource(classpathFontFile).getInputStream()) {
            final Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);

            final Map<String, String> metadata = new LinkedHashMap<>();
            metadata.put("name", font.getName());
            metadata.put("fontName", font.getFontName());
            metadata.put("family", font.getFamily());
            metadata.put("psName", font.getPSName());

            log.info("Registered custom font from path [{}]. Metadata: {}",
                     classpathFontFile, metadata);

            return font.deriveFont(Font.PLAIN, 16F);
        } catch (Exception e) {
            log.error("Failed to register custom font: {}", classpathFontFile, e);

            throw new RuntimeException("Failed to load font: " + classpathFontFile, e);
        }
    }
}