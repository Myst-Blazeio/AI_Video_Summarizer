package com.project.AIStudio;

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
        try {
            String videoId = extractVideoId(youtubeUrl);
            String audioFile = downloadAudio(videoId);
            if (audioFile == null) {
                return null;
            }

            return transcribeAudio(audioFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractVideoId(String url) {
        return url.split("v=")[1].split("&")[0];
    }

    private String downloadAudio(String videoId) {
        try {
            String outputDir = "src/main/resources/OutputFile";
            File dir = new File(outputDir);
            
            // Delete existing audio files before downloading a new one
            if (dir.exists() && dir.isDirectory()) {
                for (File file : dir.listFiles()) {
                    if (file.getName().endsWith(".mp3")) {
                        file.delete();
                    }
                }
            }

            String outputFile = outputDir + "/" + videoId + ".mp3";
            System.out.println("📥 Downloading audio to: " + outputFile);

            ProcessBuilder pb = new ProcessBuilder(
                "yt-dlp", "--extract-audio", "--audio-format", "mp3",
                "-o", outputFile, "https://www.youtube.com/watch?v=" + videoId
            );
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();

            File file = new File(outputFile);
            return file.exists() ? outputFile : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private String transcribeAudio(String audioFile) {
        try {
            System.out.println("🎙️ Transcribing audio: " + audioFile);

            ProcessBuilder pb = new ProcessBuilder("python", "scripts/transcribe.py", audioFile);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            StringBuilder transcription = new StringBuilder();
            boolean startCapturing = false;

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Python Output: " + line);

                if (line.contains("Transcription complete!")) {
                    startCapturing = true;
                    continue;
                }

                if (startCapturing) {
                    transcription.append(line).append("\n");
                }
            }

            process.waitFor();
            return transcription.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    void saveToPdf(String text, String filePath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);

                float leading = 15f; // Line spacing
                int maxLineLength = 80;

                // ✅ Remove unsupported characters (emojis, special symbols, etc.)
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
