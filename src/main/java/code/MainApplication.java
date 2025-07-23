package code;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {

        testTempFolderWriteAccess();

        SpringApplication.run(MainApplication.class, args);
    }



    private static void testTempFolderWriteAccess() {
        try {
            final File file = new File("/tmp/file.txt");

            FileUtils.writeStringToFile(file, "Hello world", UTF_8);

            System.out.println(FileUtils.readFileToString(file, UTF_8));
            System.out.println("Temp folder allows write access.");
        } catch (Exception e) {
            System.exit(1);
        }
    }
}