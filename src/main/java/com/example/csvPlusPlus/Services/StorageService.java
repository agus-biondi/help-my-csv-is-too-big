package main.java.com.example.csvPlusPlus.Services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import main.java.com.example.csvPlusPlus.DataModels.CsvMetaData;
import main.java.com.example.csvPlusPlus.Utilities.EncodingConverter;
import main.java.com.example.csvPlusPlus.Utilities.Utilites;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Service
public class StorageService {

    @Value("${cloud.application.bucket.name}")
    private String s3BucketName;

    @Autowired
    private AmazonS3 s3Client;

    public StorageService() {

    }

    public String getS3BucketName() {
        return s3BucketName;
    }

    public String uploadCsv(MultipartFile multipartFile, String delimiter) {

        String fileName = multipartFile.getOriginalFilename() + UUID.randomUUID();
        EncodingConverter enc = new EncodingConverter();

        File csv = null;
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            Map<String, String> userMetaData = new TreeMap<>();
            userMetaData.put("delimiter", delimiter);
            metadata.setUserMetadata(userMetaData);

            csv = enc.convertToUTF8(convertMultiPartFileToFile(multipartFile));

            PutObjectRequest putRequest =  new PutObjectRequest(s3BucketName, fileName, csv).withMetadata(metadata);
            s3Client.putObject(putRequest);
            csv.delete();
        } catch (Exception e) {
            e.printStackTrace();
            if (csv != null && csv.exists()) {
                csv.delete();
            }
        }
        return fileName;
    }

    //TODO HANDLE DUPLICATE HEADER NAMES
    //TODO test file with delimiter inside value
    public CsvMetaData getCsvMetaData(String fileName) {

        S3Object s3Object = s3Client.getObject(s3BucketName, fileName);
        ObjectMetadata m = s3Object.getObjectMetadata();
        String delimiter = m.getUserMetadata().get("delimiter");

        long sizeInKb = m.getContentLength();

        SelectRecordsInputStream headersInputStream = queryCSV(fileName, "SELECT * FROM s3Object s LIMIT 1", delimiter);

        List<String> headers = Arrays.asList(
                Utilites.SelectRecordsInputStreamToString(headersInputStream)
                        .split(delimiter));

        SelectRecordsInputStream countRowsInputStream = queryCSV(fileName, "SELECT COUNT(*) FROM s3Object s", delimiter);
        int rowCount = Integer.parseInt(
                Utilites.SelectRecordsInputStreamToString(countRowsInputStream)
                .trim()) - 1;

        return new CsvMetaData.Builder()
                .withRowCount(rowCount)
                .withSizeInKb(sizeInKb)
                .withHeaders(headers)
                .withFileName(fileName)
                .build();
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return convertedFile;
    }

    private SelectRecordsInputStream queryCSV(String fileName, String query, String delimiter) {
        SelectObjectContentResult result = s3Client.selectObjectContent(
                new SelectObjectContentRequest()
                        .withBucketName(s3BucketName)
                        .withKey(s3Client.getObject(s3BucketName, fileName).getKey())
                        .withExpression(query)
                        .withExpressionType(ExpressionType.SQL)
                        .withInputSerialization(new InputSerialization().withCsv(new CSVInput()
                                .withFieldDelimiter(",")
                                .withRecordDelimiter('\n')))
                        .withOutputSerialization(new OutputSerialization().withCsv(new CSVOutput()
                                .withFieldDelimiter(",")
                                .withRecordDelimiter('\n'))
                        )

        );
        return result.getPayload().getRecordsInputStream();

    }

    public List<String> getColumnSynopsis(String fileName, int columnIndex) {

        String query = String.format("SELECT * FROM s._%d", columnIndex);
        //TODO DELIMITER should be a class field....
        String columnData = Utilites.SelectRecordsInputStreamToString(queryCSV(fileName, query, ","));
        return null;
    }

    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(s3BucketName, fileName);
        String delimiter = s3Object.getObjectMetadata().getUserMetaDataOf("delimiter");

        //SelectRecordsInputStream


        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
