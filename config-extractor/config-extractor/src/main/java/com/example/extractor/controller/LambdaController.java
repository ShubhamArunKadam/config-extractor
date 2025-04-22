package com.example.extractor.controller;

import com.example.extractor.LambdaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LambdaController {

    @Autowired
    private LambdaService lambdaService;

    @GetMapping("/extract/lambda")
    public String extractLambdaConfigs() {
        lambdaService.extractAllLambdas();
        return "✅ Lambda configs extracted and uploaded to S3!";
    }
}

