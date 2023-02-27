package main.java.com.example.csvPlusPlus.Controllers;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.waiters.AmazonS3Waiters;
import lombok.extern.slf4j.Slf4j;
import main.java.com.example.csvPlusPlus.DataModels.CsvMetaData;
import main.java.com.example.csvPlusPlus.Services.StorageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
public class UploadController {

    private final StorageService storageService;

    public UploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String redirect() {
        return "redirect:/upload";
    }
    @GetMapping("/upload")
    public ModelAndView getDashboardView() {
        ModelAndView modelAndView = new ModelAndView("upload");
        modelAndView.addObject("title", "Help! My CSV is too big!");
        modelAndView.addObject("heading", "Step 1: Upload your data");
        return modelAndView;
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("delimiter") String delimiter,
                                   RedirectAttributes redirectAttributes) {

        System.out.println(delimiter);
        String thisFileName = storageService.uploadCsv(file, delimiter);
        return "redirect:/editor/" + thisFileName;

    }
}
