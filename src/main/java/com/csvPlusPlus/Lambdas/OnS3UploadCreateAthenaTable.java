package main.java.com.csvPlusPlus.Lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.athena.model.StartQueryExecutionResponse;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.StartQueryExecutionRequest;
import software.amazon.awssdk.services.athena.model.QueryExecutionContext;
import software.amazon.awssdk.services.athena.model.ResultConfiguration;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class OnS3UploadCreateAthenaTable implements RequestHandler<S3Event, String> {

    private final AthenaClient athena = AthenaClient.create();
    private final S3AsyncClient s3AsyncClient = S3AsyncClient.create();
    private LambdaLogger logger;

    public OnS3UploadCreateAthenaTable() {
    }

    @Override
    public String handleRequest(S3Event event, Context context) {
        this.logger = context.getLogger();

        String bucket = event.getRecords().get(0).getS3().getBucket().getName();
        String key = event.getRecords().get(0).getS3().getObject().getKey();

        logger.log(String.format("File uploaded to bucket: %s, with key: %s", bucket, key));

        // Get the custom delimiter from the S3 file metadata
        String delimiter = null;
        try {
            delimiter = s3AsyncClient.headObject(
                            HeadObjectRequest.builder()
                                    .bucket(bucket)
                                    .key(key)
                                    .build())
                    .join()
                    .metadata()
                    .get("delimiter");
            logger.log(String.format("delimiter found: %s", delimiter));
        } catch (Exception e) {
            logger.log(String.format("delimiter not found, using ',': %s", e.getMessage()));
            delimiter = ",";
        }

        // Request to get the first line of the CSV file
        SelectObjectContentRequest request = SelectObjectContentRequest.builder()
                .bucket(bucket)
                .key(key)
                .expressionType(ExpressionType.SQL)
                .expression("SELECT * FROM s3object s LIMIT 1")
                .inputSerialization(InputSerialization.builder()
                        .csv(CSVInput.builder()
                                .fileHeaderInfo(FileHeaderInfo.NONE)
                                .fieldDelimiter(delimiter)
                                .recordDelimiter("\n")
                                .build())
                        .build())
                .outputSerialization(OutputSerialization.builder()
                        .csv(CSVOutput.builder()
                                .fieldDelimiter(delimiter)
                                .recordDelimiter("\n")
                                .build())
                        .build())
                .build();

        StringBuilder columnNameBuilder = new StringBuilder();
        String columnNames = null;

        SelectObjectContentResponseHandler.Visitor visitor = SelectObjectContentResponseHandler.Visitor
                .builder().onRecords(recordsEvent -> {
                    logger.log(String.format("onRecords Called"));
                    SdkBytes bytes = recordsEvent.payload();
                    String result = new String(bytes.asByteArray(), Charset.forName("UTF-8"));
                    columnNameBuilder.append(result);
                }).build();

        SelectObjectContentResponseHandler responseHandler = SelectObjectContentResponseHandler.builder()
                .onEventStream(e -> {
                    e.subscribe(s -> s.accept(visitor));
                })
                .build();

        // Execute request and get the first line of the CSV file
        try {
            CompletableFuture<Void> future = s3AsyncClient.selectObjectContent(request, responseHandler);
            future.join();
            columnNames = columnNameBuilder.toString().trim();
            logger.log(String.format("file headers: %s", columnNames));
        } catch (Exception e) {
            logger.log(String.format("Error reading first line of CSV file: %s %s", e.getMessage(), e.getStackTrace()[0]));
            return null;
        }

        String[] columns = uniqueColumnNames(columnNames.split(","));
        StringBuilder columnDefinitions = new StringBuilder();
        for (String columnName : columns) {
            columnDefinitions.append("\t`")
                    .append(sanitizeToAthenaCompliantName(columnName))
                    .append("` string,\n");
        }

        // Remove trailing comma and newline
        columnDefinitions.setLength(columnDefinitions.length() - 2);

        String databaseName = "help-my-csv-is-too-big";
        String tableName = key.substring(key.lastIndexOf("/") + 1);
        tableName = sanitizeToAthenaCompliantName(tableName);
        String folderPath = key.substring(0, key.lastIndexOf("/"));
        String query = String.format("CREATE EXTERNAL TABLE IF NOT EXISTS `%s`.`%s` (\n%s\n)" +
                        "ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde' " +
                        "WITH SERDEPROPERTIES ( 'separatorChar' = '%s', 'quoteChar' = '\\'', 'escapeChar' = '\\\\' ) \n" +
                        "STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat' \n" +
                        "OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat' \n" +
                        "LOCATION 's3://%s/%s/' \n" +
                        "TBLPROPERTIES ( 'classification'='csv' )",
                databaseName, tableName, columnDefinitions, ",", databaseName, folderPath);

        StartQueryExecutionRequest startQueryExecutionRequest = StartQueryExecutionRequest.builder()
                .queryString(query)
                .queryExecutionContext(QueryExecutionContext.builder()
                        .database(databaseName)
                        .build())
                .resultConfiguration(ResultConfiguration.builder()
                        .outputLocation("s3://help-my-csv-is-too-big/athena-output")
                        .build())
                .build();

        StartQueryExecutionResponse startQueryExecutionResponse = athena.startQueryExecution(startQueryExecutionRequest);

        return startQueryExecutionResponse.queryExecutionId();


    }

    private static String removeFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return filename;
        }
        return filename.substring(0, filename.lastIndexOf("."));
    }

    private String sanitizeToAthenaCompliantName(String unsanitizedString) {
        String toUnderscoreRegex = "[\\s-]";
        unsanitizedString.replaceAll(toUnderscoreRegex, "_");

        String regex = "[^a-zA-Z0-9_]";
        return unsanitizedString.replaceAll(regex, "");
    }

    private String[] uniqueColumnNames(String[] columnNames) {
        Map<String, Integer> fileNames = new HashMap<>();

        for (int i = 0; i < columnNames.length; i++) {
            String fileName = columnNames[i];

            if (fileNames.containsKey(fileName)) {
                int k = fileNames.get(fileName) + 1;

                String newFileName = String.format("%s_%d_", fileName, k);
                while (fileNames.containsKey(newFileName)) {
                    k++;
                    newFileName = String.format("%s_%d_", fileName, k);
                }

                fileNames.put(fileName, k);
                fileNames.put(newFileName, 0);
                columnNames[i] = newFileName;
            } else {
                fileNames.put(fileName, 0);
            }
        }
        return columnNames;
    }
}
