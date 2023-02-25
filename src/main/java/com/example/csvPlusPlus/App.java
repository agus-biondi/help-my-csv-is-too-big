package main.java.com.example.csvPlusPlus;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class App {

    private final AmazonS3 amazonS3Client;

    public App(AmazonS3 amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @EventListener(classes = ApplicationReadyEvent.class)
    public void onApplicationReadyEvent(ApplicationReadyEvent event) {
        /*
        for (Bucket availableBuckets : amazonS3Client.listBuckets()) {
            System.out.println(availableBuckets.getName());
        }
        */
    }
}