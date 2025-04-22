package com.example.extractor;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.lexmodelbuilding.AmazonLexModelBuilding;
import com.amazonaws.services.lexmodelbuilding.AmazonLexModelBuildingClientBuilder;
import com.amazonaws.services.lexmodelbuilding.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class LexV1DeploymentService {

    public void deployLex(JsonNode botConfig, BasicSessionCredentials tempCreds) throws Exception {
        AmazonLexModelBuilding lexClient = AmazonLexModelBuildingClientBuilder.standard()
                .withRegion("us-east-1")
                .withCredentials(new AWSStaticCredentialsProvider(tempCreds))
                .build();

        String botName = botConfig.get("botName").asText();
        String description = botConfig.get("description").asText();

        List<Intent> intentList = new ArrayList<>();
        Iterator<JsonNode> intents = botConfig.get("intents").elements();

        while (intents.hasNext()) {
            JsonNode intentNode = intents.next();
            String intentName = intentNode.get("name").asText();

            List<String> utterances = new ArrayList<>();
            for (JsonNode utt : intentNode.get("sampleUtterances")) {
                utterances.add(utt.asText());
            }

            PutIntentRequest intentReq = new PutIntentRequest()
                    .withName(intentName)
                    .withSampleUtterances(utterances)
                    .withConclusionStatement(new Statement().withMessages(
                            new Message()
                                    .withContentType("PlainText")
                                    .withContent("Thank you!")
                    ));

            PutIntentResult intentResult = lexClient.putIntent(intentReq);

            intentList.add(new Intent()
                    .withIntentName(intentName)
                    .withIntentVersion("$LATEST"));
        }

        PutBotRequest botRequest = new PutBotRequest()
                .withName(botName)
                .withDescription(description)
                .withLocale("en-US")
                .withChildDirected(false)
                .withIntents(intentList)
                .withProcessBehavior("SAVE");

        lexClient.putBot(botRequest);

        System.out.println("✅ Lex Bot deployed: " + botName);
    }
}

