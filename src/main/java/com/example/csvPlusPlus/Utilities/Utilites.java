package main.java.com.example.csvPlusPlus.Utilities;

import com.amazonaws.services.s3.model.SelectRecordsInputStream;
import com.amazonaws.util.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utilites {


    public static String selectRecordsInputStreamToString(SelectRecordsInputStream inputStream){

        String records = "";
        try {
            records = IOUtils.toString(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return records;
    }

    public static File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(System.getProperty("java.io.tmpdir") + "/"+ file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return convertedFile;
    }

}
