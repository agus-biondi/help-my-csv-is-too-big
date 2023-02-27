package main.java.com.example.csvPlusPlus.Controllers;

import main.java.com.example.csvPlusPlus.DataModels.CsvMetaData;
import main.java.com.example.csvPlusPlus.Services.StorageService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

@Controller
public class EditorController {

    private final StorageService storageService;

    public EditorController(StorageService storageService) {
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
        modelAndView.addObject("fileName", fileName);

        Form f = new Form();
        f.setToggles(Arrays.asList("1","2"));
        modelAndView.addObject("title", "Editor");
        modelAndView.addObject("heading", "Step 2: Choose your data");
        modelAndView.addObject("toggles", metaData.getHeaders());
        modelAndView.addObject("metaData", metaData.getFormattedMetaData());
        modelAndView.addObject("form", f);
        return modelAndView;
    }

    @PostMapping("/editor/{fileName}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String fileName,
                                                      @RequestParam(value = "toggle", required = false) List<Integer> selectedColumnNumbers) {

        byte[] data = storageService.downloadFile(fileName, selectedColumnNumbers);
        ByteArrayResource resource = new ByteArrayResource(data);
        String downloadFileName = fileName.substring(0, fileName.length() - 36);
        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + downloadFileName + "not_as_big_as_it_once_was" + "\"")
                .body(resource);
    }

    /*
    @GetMapping("/editor/${filename}/getColumnSynopsis/${columnIndex}")
    @ResponseBody
    public List<String> getColumnSynopsis(@PathVariable String fileName, @PathVariable int columnIndex) {

        return storageService.getColumnSynopsis(fileName, columnIndex);

    }
    */


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
