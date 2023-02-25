package main.java.com.example.csvPlusPlus;

import java.io.*;
import java.nio.charset.Charset;

import com.ibm.icu.text.CharsetDetector;


public class EncodingConverter {

    public EncodingConverter() {

    }

    public File convertToUTF8(File inputFile) throws Exception {


        FileInputStream inputStream = new FileInputStream(inputFile);
        CharsetDetector detector = new CharsetDetector();
        detector.setText(new BufferedInputStream(inputStream));
        String fileEncoding = detector.detect().getName();


        if (fileEncoding.equals("UTF-8")) {
            return inputFile;
        }

        Charset inputCharset = Charset.forName(fileEncoding);
        Charset outputCharset = Charset.forName("UTF-8");

        File outputFile = new File(inputFile.getName() + "temp");

        InputStreamReader reader = new InputStreamReader(new FileInputStream(inputFile), inputCharset);
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile), outputCharset);

        // Read the input file and write the converted data to the output file
        char[] buffer = new char[4096];
        int read;
        while ((read = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, read);
        }

        // Close the readers and writers
        reader.close();
        writer.close();


        return outputFile;
    }



}
