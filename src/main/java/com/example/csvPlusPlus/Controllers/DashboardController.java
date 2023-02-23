package com.example.csvPlusPlus.Controllers;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DashboardController {

    private final String s3BucketName;
    private final AmazonS3 amazonS3Client;
    private final String bucketLocation;

    public DashboardController(AmazonS3 amazonS3Client) {
        this.s3BucketName = "csv-test-big-thing";
        this.amazonS3Client = amazonS3Client;
        this.bucketLocation = amazonS3Client.getBucketLocation(this.s3BucketName);
    }

    @GetMapping("/")
    public ModelAndView getDashboardView() {
        ModelAndView modelAndView = new ModelAndView("dashboard");
        modelAndView.addObject("bucketName", s3BucketName);
        //modelAndView.addObject("bucketLocation", bucketLocation);
        //modelAndView.addObject("availableFiles", amazonS3Client.listObjects(s3BucketName).getObjectSummaries());
        return modelAndView;
    }
}
