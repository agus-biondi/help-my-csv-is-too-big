package main.java.com.example.csvPlusPlus.Utilities;

import com.amazonaws.services.s3.model.SelectRecordsInputStream;
import com.amazonaws.util.IOUtils;

public class Utilites {

    public static String SelectRecordsInputStreamToString(SelectRecordsInputStream inputStream){

        String records = "";
        try {
            records = IOUtils.toString(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return records;
    }
}
