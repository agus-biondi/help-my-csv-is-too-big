package main.java.com.csvPlusPlus;

import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {

    private final S3Client s3Client;

    public App(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}