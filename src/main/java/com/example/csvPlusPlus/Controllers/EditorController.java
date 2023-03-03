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


    @GetMapping("/editor/{fileName}")
    public ModelAndView getEditorView(@PathVariable String fileName){

        CsvMetaData metaData = storageService.getCsvMetaData(fileName);

        ModelAndView modelAndView = new ModelAndView("editor");
        modelAndView.addObject("title", "Help! My CSV is too big!");
        modelAndView.addObject("fileName", fileName);
        modelAndView.addObject("heading", "Step 2: Choose your columns");
        modelAndView.addObject("toggles", metaData.getHeaders());
        modelAndView.addObject("metaData", metaData.getFormattedMetaData());
        return modelAndView;
    }

    @GetMapping("/editor/{fileName}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String fileName,
                                                      @RequestParam(value = "toggle", required = false) List<Integer> selectedColumnNumbers) {

        byte[] data = storageService.downloadFile(fileName, selectedColumnNumbers);
        ByteArrayResource resource = new ByteArrayResource(data);
        String downloadFileName = fileName.substring(0, fileName.length() - 36);
        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + "not_as_big_as_it_once_was_" + downloadFileName + "\"")
                .body(resource);
    }

    /*
    //Column data aggregations - outside scope of project
    @GetMapping("/editor/${filename}/getColumnSynopsis/${columnIndex}")
    @ResponseBody
    public List<String> getColumnSynopsis(@PathVariable String fileName, @PathVariable int columnIndex) {

        return storageService.getColumnSynopsis(fileName, columnIndex);

    }
    */

}
