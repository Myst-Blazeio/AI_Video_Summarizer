package com.project.AIStudio;

import java.io.IOException;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the YouTube URL:");
        String youtubeUrl = scanner.nextLine();
        scanner.close();

        // Step 1: Transcribe YouTube Video
        YouTubeTranscriber ytTranscriber = new YouTubeTranscriber();
        String transcription = ytTranscriber.transcribeFromYouTube(youtubeUrl);

        if (transcription == null || transcription.isEmpty()) {
            System.err.println("❌ Error: Transcription failed.");
            return;
        }
        
        ytTranscriber.saveToPdf(transcription, "src/main/resources/OutputFile/speech.pdf");
        System.out.println("✅ Saved transcription to speech.pdf");

        // Step 2: Summarize Transcription
        GoogleAISummarizer summarizer = new GoogleAISummarizer();
        String summary = summarizer.generateSummary(transcription);
        System.out.println("\n🔹 AI Summary: " + summary);

        // Step 3: Convert Summary to PDF
        String outputFilePath = "src/main/resources/OutputFile/summary.pdf";
        TextToPDFConverter pdfConverter = new TextToPDFConverter();

        try {
            pdfConverter.createPdf("Lecture Summary", summary, outputFilePath);
            System.out.println("✅ PDF created successfully: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("❌ Error generating PDF: " + e.getMessage());
        }
    }
}