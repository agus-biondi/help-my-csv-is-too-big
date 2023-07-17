package main.java.com.example.csvPlusPlus.Lambdas;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.StartCrawlerRequest;
import software.amazon.awssdk.services.glue.model.GlueException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class StartS3Crawler implements RequestHandler<Object, String> {


    public String handleRequest(Object input, Context context) {
        GlueClient glue = GlueClient.create();
        String crawlerName = "s3-crawler-help-my-csv-is-too-big";

        try {
            StartCrawlerRequest startCrawlerRequest = StartCrawlerRequest.builder()
                    .name(crawlerName)
                    .build();
            glue.startCrawler(startCrawlerRequest);
            return String.format("Started crawler: %s", crawlerName);
        } catch (GlueException e) {
            return String.format("Error starting crawler: %s", e.awsErrorDetails().errorMessage());
        }
    }
}
