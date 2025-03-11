package com.project.aisummarizer.LectureSummarizer;

import java.io.IOException;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the text to summarize:");
        String inputText = scanner.nextLine();
        scanner.close();

        TextToPDFConverter ttpc = new TextToPDFConverter();
        GoogleAISummarizer gais = new GoogleAISummarizer();

        // 🔹 Step 1: Get AI Summary
        String summary = gais.generateSummary(inputText);
        System.out.println("\n🔹 AI Summary: " + summary);

        // 🔹 Step 2: Generate PDF with Summary
        String filePath = "src/main/resources/OutputFile/output.pdf";
        try {
            ttpc.createPdf("Lecture Summary", summary, filePath);
            System.out.println("✅ PDF created successfully: " + filePath);
        } catch (IOException e) {
            System.err.println("❌ Error generating PDF: " + e.getMessage());
        }
    }
}
