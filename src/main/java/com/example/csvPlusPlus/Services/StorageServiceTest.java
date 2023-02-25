package main.java.com.example.csvPlusPlus.Services;

import main.java.com.example.csvPlusPlus.DataModels.CsvMetaData;
import main.java.com.example.csvPlusPlus.Services.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class StorageServiceTest {



    private StorageService service = new StorageService();

    @Test
    public void getCsvMetaData_existingFile_returnsMetaData() {

        String fileName = "admob-report (2).csvfe444818-7150-4215-a661-6b7ccf173798";

        System.out.println(service.getS3BucketName());
        //CsvMetaData m = service.getCsvMetaData(fileName);

        //System.out.println(m.getSizeInMb());

    }


}
