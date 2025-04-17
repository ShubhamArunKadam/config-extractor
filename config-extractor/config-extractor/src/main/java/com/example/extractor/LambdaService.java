package com.example.extractor;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LambdaService {

    @Autowired
    private S3Uploader s3Uploader;

    public void extractAllLambdas() {
        AWSLambda lambdaClient = AWSLambdaClientBuilder.defaultClient();
        ListFunctionsResult result = lambdaClient.listFunctions();

        List<FunctionConfiguration> functions = result.getFunctions();
        for (FunctionConfiguration function : functions) {
            Map<String, Object> config = new HashMap<>();
            config.put("functionName", function.getFunctionName());
            config.put("runtime", function.getRuntime());
            config.put("handler", function.getHandler());
            config.put("memorySize", function.getMemorySize());
            config.put("timeout", function.getTimeout());

            String timestamp = Instant.now().toString();
            String fileName = "configs/lambda/" + function.getFunctionName() + "-" + timestamp + ".json";

            s3Uploader.uploadJson(config, fileName);
        }
    }
}

