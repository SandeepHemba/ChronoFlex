package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.service.HuggingFaceService;
import com.example.ChronoFlex.service.ProjectDocumentService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class ChronoFlexAIController {

    private final HuggingFaceService hfService;
    private final ProjectDocumentService documentService;

    private final List<Map<String,String>> chatHistory = new ArrayList<>();

    public ChronoFlexAIController(HuggingFaceService hfService,
                                  ProjectDocumentService documentService) {
        this.hfService = hfService;
        this.documentService = documentService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String message) {

        String systemPrompt = """
        You are the official AI assistant of ChronoFlex.

        Rules:
        1. Only answer questions related to ChronoFlex.
        2. Use only the provided documentation.
        3. Do not invent information.
        4. If unrelated question reply:
           "I can only provide information about ChronoFlex software."

        Keep answers short (max 6 lines).
        """;

        String pdfContent = documentService.getPdfText();

        String context =
                systemPrompt +
                        "\n\nProject Documentation:\n" +
                        pdfContent;

        chatHistory.add(Map.of("role","user","content",message));

        if(chatHistory.size() > 6){
            chatHistory.remove(0);
        }

        String response = hfService.getResponse(context, chatHistory);

        chatHistory.add(Map.of("role","assistant","content",response));

        return response;
    }
}