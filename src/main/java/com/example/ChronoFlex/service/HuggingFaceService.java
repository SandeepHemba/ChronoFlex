package com.example.ChronoFlex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class HuggingFaceService {

    @Value("${hf.api.token}")
    private String token;

    @Value("${hf.model.url}")
    private String modelUrl;

    @Value("${hf.model.name}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getResponse(String systemPrompt, List<Map<String,String>> history){

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<Map<String,String>> messages = new ArrayList<>();

        messages.add(Map.of("role","system","content",systemPrompt));
        messages.addAll(history);

        Map<String,Object> body = new HashMap<>();

        body.put("model",modelName);
        body.put("temperature",0.3);
        body.put("max_tokens",300);
        body.put("messages",messages);

        HttpEntity<Map<String,Object>> request =
                new HttpEntity<>(body,headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(modelUrl,request,String.class);

        try{

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            return root.get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();

        }catch(Exception e){
            return "Error parsing AI response.";
        }
    }
}