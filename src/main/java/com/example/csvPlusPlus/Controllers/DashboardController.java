package main.java.com.example.csvPlusPlus.Controllers;

import com.amazonaws.services.s3.AmazonS3;
import main.java.com.example.csvPlusPlus.DataModels.CsvMetaData;
import main.java.com.example.csvPlusPlus.Services.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

@Controller
public class DashboardController {

    List<String> defaultToggles = Arrays.asList(
            "Option 1", "Option 2", "Option 3", "Option 4", "Option 5",
            "Option 6", "Option 7", "Option 8", "Option 9", "Option 10",
            "Option 11", "Option 12", "Option 13", "Option 14", "Option 15"
    );

    private final StorageService storageService;


    public DashboardController(StorageService storageService) {
        this.storageService = storageService;
    }

    /*
    @GetMapping("/")
    public ModelAndView getDashboardView() {
        ModelAndView modelAndView = new ModelAndView("dashboard");
        modelAndView.addObject("bucketName", s3BucketName);
        modelAndView.addObject("availableFiles", amazonS3Client.listObjects(s3BucketName).getObjectSummaries());
        return modelAndView;
    } */

    @GetMapping("/editor/{fileName}")
    public ModelAndView getEditorView(@PathVariable String fileName){

        CsvMetaData metaData = storageService.getCsvMetaData(fileName);

        ModelAndView modelAndView = new ModelAndView("editor");
        modelAndView.addObject("title", "title");


        Form f = new Form();
        f.setToggles(Arrays.asList("1","2"));
        modelAndView.addObject("title", "Form Page");
        modelAndView.addObject("heading", "Step 2: Choose your data");
        modelAndView.addObject("toggles", metaData.getHeaders());
        modelAndView.addObject("metaData", metaData.getFormattedMetaData());
        modelAndView.addObject("form", f);
        return modelAndView;
    }

    public static class Form {
        private List<String> toggles;

        public List<String> getToggles() {
            return toggles;
        }

        public void setToggles(List<String> toggles) {
            this.toggles = toggles;
        }
    }
}
