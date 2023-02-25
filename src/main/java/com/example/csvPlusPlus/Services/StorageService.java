package main.java.com.example.csvPlusPlus.Services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import main.java.com.example.csvPlusPlus.DataModels.CsvMetaData;
import main.java.com.example.csvPlusPlus.EncodingConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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

    public String uploadCsv(MultipartFile multipartFile) {

        String fileName = multipartFile.getOriginalFilename() + UUID.randomUUID();
        EncodingConverter enc = new EncodingConverter();

        File csv;
        try {
            csv = enc.convertToUTF8(convertMultiPartFileToFile(multipartFile));
            PutObjectRequest putRequest =  new PutObjectRequest(s3BucketName, fileName, csv);
            s3Client.putObject(putRequest);
            csv.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public CsvMetaData getCsvMetaData(String fileName) {

        S3Object s3Object = s3Client.getObject(s3BucketName, fileName);
        ObjectMetadata m = s3Object.getObjectMetadata();


        long sizeInKb = m.getContentLength();

        //TODO set delimeter when uploading
        String delimeter = ",";
        try {
            delimeter = m.getUserMetadata().get("DELIMETER");
            if (delimeter == null) {
                delimeter = ",";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> headers = Arrays.asList(
                queryCSV(fileName, "SELECT * FROM s3Object s LIMIT 1").split(delimeter));

        int rowCount = Integer.parseInt(queryCSV(fileName, "SELECT COUNT(*) FROM s3Object s").trim()) - 1;

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

        }
        return convertedFile;
    }

    public String queryCSV(String fileName, String query) {
        SelectObjectContentResult result = s3Client.selectObjectContent(
                new SelectObjectContentRequest()
                        .withBucketName(s3BucketName)
                        .withKey(s3Client.getObject(s3BucketName, fileName).getKey())
                        .withExpression(query)
                        .withExpressionType(ExpressionType.SQL)
                        .withInputSerialization(new InputSerialization().withCsv(new CSVInput()
                                .withFieldDelimiter(',')
                                .withRecordDelimiter('\n')))
                        .withOutputSerialization(new OutputSerialization().withCsv(new CSVOutput()
                                .withFieldDelimiter(',')
                                .withRecordDelimiter('\n'))
                        )

        );
        SelectRecordsInputStream s = result.getPayload().getRecordsInputStream();
        String records = "";
        try {
            records = IOUtils.toString(s);
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return records;
    }

}
