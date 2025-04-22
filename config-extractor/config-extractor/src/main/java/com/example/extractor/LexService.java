package com.example.extractor;

import com.amazonaws.services.lexmodelbuilding.AmazonLexModelBuilding;
import com.amazonaws.services.lexmodelbuilding.AmazonLexModelBuildingClientBuilder;
import com.amazonaws.services.lexmodelbuilding.model.GetIntentRequest;
import com.amazonaws.services.lexmodelbuilding.model.GetIntentResult;
import com.amazonaws.services.lexmodelbuilding.model.GetBotRequest;
import com.amazonaws.services.lexmodelbuilding.model.GetBotResult;
import com.amazonaws.services.lexmodelbuilding.model.IntentMetadata;
import com.amazonaws.services.lexmodelbuilding.model.GetIntentsRequest;
import com.amazonaws.services.lexmodelbuilding.model.GetIntentsResult;
import com.amazonaws.services.lexmodelbuilding.model.GetBotsRequest;
import com.amazonaws.services.lexmodelbuilding.model.GetBotsResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class LexService {

    @Autowired
    private S3Uploader s3Uploader;

    private final AmazonLexModelBuilding lexClient = AmazonLexModelBuildingClientBuilder.defaultClient();

    public void extractAllLexBots() {
        GetBotsRequest getBotsRequest = new GetBotsRequest().withMaxResults(10);
        GetBotsResult botsResult = lexClient.getBots(getBotsRequest);

        for (com.amazonaws.services.lexmodelbuilding.model.BotMetadata bot : botsResult.getBots()) {
            String botName = bot.getName();

            Map<String, Object> botConfig = new HashMap<>();
            botConfig.put("botName", botName);
            botConfig.put("description", bot.getDescription());
            botConfig.put("status", bot.getStatus());
            botConfig.put("lastUpdated", bot.getLastUpdatedDate().toString());

            // Get intents for this bot
            GetIntentsRequest intentsRequest = new GetIntentsRequest().withMaxResults(10);
            GetIntentsResult intentsResult = lexClient.getIntents(intentsRequest);

            List<Map<String, Object>> intentList = new ArrayList<>();
            for (IntentMetadata intentMeta : intentsResult.getIntents()) {
                GetIntentRequest intentRequest = new GetIntentRequest()
                        .withName(intentMeta.getName())
                        .withVersion(intentMeta.getVersion());
                GetIntentResult intent = lexClient.getIntent(intentRequest);

                Map<String, Object> intentMap = new HashMap<>();
                intentMap.put("name", intent.getName());
                intentMap.put("version", intent.getVersion());
                intentMap.put("sampleUtterances", intent.getSampleUtterances());
                intentMap.put("confirmationPrompt", intent.getConclusionStatement());

                intentList.add(intentMap);
            }

            botConfig.put("intents", intentList);

            String timestamp = Instant.now().toString();
            String s3Key = String.format("configs/lex/%s-%s.json", botName, timestamp);
            s3Uploader.uploadJson(botConfig, s3Key);
        }
    }
}

