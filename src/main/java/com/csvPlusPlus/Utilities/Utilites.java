package main.java.com.csvPlusPlus.Utilities;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class Utilites {


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
