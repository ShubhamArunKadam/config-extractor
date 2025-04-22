package com.example.extractor.controller;

import com.example.extractor.LexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LexController {

    @Autowired
    private LexService lexService;

    @GetMapping("/extract/lex")
    public String extractLexConfigs() {
        lexService.extractAllLexBots();
        return "✅ Lex configs extracted and uploaded to S3!";
    }
}

