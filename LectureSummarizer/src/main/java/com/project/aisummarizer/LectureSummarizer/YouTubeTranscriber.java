package com.project.aisummarizer.LectureSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class YouTubeTranscriber {

    public String transcribeFromYouTube(String youtubeUrl) {
        System.out.println("🔗 Video URL received: " + youtubeUrl);
        String videoId = extractVideoId(youtubeUrl);
        System.out.println("🔍 Video ID extracted: " + videoId);
        
        if (videoId == null || videoId.isEmpty()) {
            System.err.println("❌ Invalid YouTube URL.");
            return "";
        }
        
        try {
            // Update the path to your python executable in the .venv folder
            String pythonExecutable = "scripts/.venv/Scripts/python"; // Adjust this based on your environment

            // Create a ProcessBuilder using the Python executable path and the script path
            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "scripts/api.py", videoId);
            System.out.println("Working..");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("❌ Python script failed.");
                return "";
            }
            System.out.println("Transcript:"+output.toString().trim());
            return output.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String extractVideoId(String url) {
        try {
            if (url.contains("v=")) {
                return url.split("v=")[1].split("&")[0];
            } else if (url.contains("youtu.be/")) {
                return url.split("youtu.be/")[1].split("\\?")[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveToPdf(String text, String filePath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);

                float leading = 15f; // Line spacing
                int maxLineLength = 80;

                // Clean unsupported characters
                text = text.replaceAll("[^\\p{Print}\\p{Space}]", "");

                String[] lines = text.split("\n");
                for (String line : lines) {
                    while (line.length() > maxLineLength) {
                        int splitIndex = line.lastIndexOf(' ', maxLineLength);
                        if (splitIndex == -1) splitIndex = maxLineLength;

                        contentStream.showText(line.substring(0, splitIndex));
                        contentStream.newLineAtOffset(0, -leading);
                        line = line.substring(splitIndex).trim();
                    }

                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -leading);
                }

                contentStream.endText();
            }

            document.save(new File(filePath));
            System.out.println("✅ Saved transcription to: " + filePath);
        }
    }
}
