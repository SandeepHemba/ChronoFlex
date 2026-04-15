package com.example.ChronoFlex.service;

import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class ProjectDocumentService {

    private String cachedPdfText;

    @PostConstruct
    public void loadPdf() {
        try {

            ClassPathResource resource = new ClassPathResource("docs/ChronoFlex_Project_Documentation.pdf");
            InputStream inputStream = resource.getInputStream();

            PDDocument document = PDDocument.load(inputStream);

            PDFTextStripper stripper = new PDFTextStripper();
            cachedPdfText = stripper.getText(document);

            document.close();

        } catch (Exception e) {
            cachedPdfText = "Project documentation could not be loaded.";
        }
    }

    public String getPdfText() {
        return cachedPdfText;
    }
}