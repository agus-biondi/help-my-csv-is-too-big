package main.java.com.csvPlusPlus.Services;

import main.java.com.csvPlusPlus.DataModels.CsvMetaData;
import main.java.com.csvPlusPlus.Utilities.EncodingConverter;
import main.java.com.csvPlusPlus.Utilities.Utilites;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.*;
import java.util.*;

@Service
public class StorageService {

    @Value("${cloud.application.bucket.name}")
    private String s3BucketName;

    @Autowired
    private S3Client s3Client;

    public StorageService() {

    }

    public String getS3BucketName() {
        return s3BucketName;
    }


    public String uploadCsv(MultipartFile multipartFile, String delimiter) {

        System.out.println("uploading...");
        String username = "agustin";
        //s3BucketName += String.format("/uploads/%s/%s", username,UUID.randomUUID());
        String fileName = multipartFile.getOriginalFilename();
        String path = String.format("uploads/%s/%s/%s", username,UUID.randomUUID(), fileName);

        EncodingConverter enc = new EncodingConverter();
System.out.println(fileName);
System.out.println(s3BucketName);
        System.out.println(path);
        File csv = null;
        try {
            Map<String, String> userMetaData = new TreeMap<>();
            userMetaData.put("delimiter", delimiter);

            //file needs to be UTF8 to query directly from S3
            csv = enc.getFileAsUTF8(Utilites.convertMultiPartFileToFile(multipartFile));

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(path)
                    .metadata(userMetaData)
                    .build();
            s3Client.putObject(putRequest, RequestBody.fromFile(csv));
            csv.delete();
        } catch (Exception e) {
            e.printStackTrace();
            if (csv != null && csv.exists()) {
                csv.delete();
            }
        }

        return fileName;
    }
    
/*
    public CsvMetaData getCsvMetaData(String fileName) {

        S3Object s3Object = s3Client.getObject(s3BucketName, fileName);
        ObjectMetadata m = s3Object.getObjectMetadata();
        String delimiter = m.getUserMetadata().get("delimiter");

        long sizeInKb = m.getContentLength();

        SelectRecordsInputStream headersInputStream = queryCSV(fileName, "SELECT * FROM s3Object s LIMIT 1", delimiter);

        //TODO Could do this once when file is uploaded, set it in userSetMeta data instead of potentially multiple times.
        List<String> headers = Arrays.asList(
                Utilites.selectRecordsInputStreamToString(headersInputStream)
                        .split(delimiter));

        SelectRecordsInputStream countRowsInputStream = queryCSV(fileName, "SELECT COUNT(*) FROM s3Object s", delimiter);
        int rowCount = Integer.parseInt(
                Utilites.selectRecordsInputStreamToString(countRowsInputStream)
                .trim()) - 1;

        return new CsvMetaData.Builder()
                .withRowCount(rowCount)
                .withSizeInKb(sizeInKb)
                .withHeaders(headers)
                .withFileName(fileName)
                .build();
    }



    private SelectRecordsInputStream queryCSV(String fileName, String query, String delimiter) {

        //TODO use enums for delimiter
        if (delimiter.equals("\\t")) {
            delimiter = "\t";
        }

        SelectObjectContentResult result = s3Client.selectObjectContent(
                new SelectObjectContentRequest()
                        .withBucketName(s3BucketName)
                        .withKey(s3Client.getObject(s3BucketName, fileName).getKey())
                        .withExpression(query)
                        .withExpressionType(ExpressionType.SQL)
                        .withInputSerialization(new InputSerialization().withCsv(new CSVInput()
                                .withFieldDelimiter(delimiter.charAt(0))
                                .withRecordDelimiter('\n')))
                        .withOutputSerialization(new OutputSerialization().withCsv(new CSVOutput()
                                .withFieldDelimiter(delimiter.charAt(0))
                                .withRecordDelimiter('\n'))
                        )

        );
        return result.getPayload().getRecordsInputStream();

    }

    public byte[] downloadFile(String fileName, List<Integer> selectedColumnNumbers) {
        S3Object s3Object = s3Client.getObject(s3BucketName, fileName);
        String delimiter = s3Object.getObjectMetadata().getUserMetaDataOf("delimiter");

        StringBuilder query = new StringBuilder("SELECT ");
        for (Integer i : selectedColumnNumbers) {
            query.append(String.format("s._%d, ", i));
        }
        query.deleteCharAt(query.lastIndexOf(","));
        query.append("FROM s3Object s");

        System.out.println(query.toString());
        SelectRecordsInputStream recordsResults = queryCSV(fileName, query.toString(), delimiter);

        try {
            byte[] content = IOUtils.toByteArray(recordsResults);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
*/
/*
    Out of project scope.
    public List<String> getColumnSynopsis(String fileName, int columnIndex) {

        String query = String.format("%d", columnIndex);
        //TODO DELIMITER could be a class field?
        String columnData = Utilites.selectRecordsInputStreamToString(queryCSV(fileName, query, ","));
        return null;
    }
*/

}