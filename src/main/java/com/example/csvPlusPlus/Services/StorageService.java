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

        //TODO what if same filenameand UUID?
        String fileName = multipartFile.getOriginalFilename() + UUID.randomUUID();
        EncodingConverter enc = new EncodingConverter();

        File csv = null;
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            Map<String, String> userMetaData = new TreeMap<>();
            userMetaData.put("delimiter", delimiter);
            metadata.setUserMetadata(userMetaData);

            //file needs to be UTF8 to query directly from S3
            csv = enc.convertToUTF8(Utilites.convertMultiPartFileToFile(multipartFile));

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


    //TODO test a field value that contains delimiter...
    public CsvMetaData getCsvMetaData(String fileName) {

        S3Object s3Object = s3Client.getObject(s3BucketName, fileName);
        ObjectMetadata m = s3Object.getObjectMetadata();
        String delimiter = m.getUserMetadata().get("delimiter");

        long sizeInKb = m.getContentLength();

        SelectRecordsInputStream headersInputStream = queryCSV(fileName, "SELECT * FROM s3Object s LIMIT 1", delimiter);

        //TODO do this once and set it in userSetMeta data instead of potentially multiple times.
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
