package com.project.transcriber;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


public class YouTubeTranscriber {
    public static void main(String[] args) {
        String youtubeUrl = "https://www.youtube.com/watch?v=1aA1WGON49E";  // Change this!

        try {
            String videoId = extractVideoId(youtubeUrl);
            String audioFile = downloadAudio(videoId);
            if (audioFile == null) {
                System.out.println("❌ Audio download failed!");
                return;
            }

            // Call Python script to transcribe audio
            String transcription = transcribeAudio(audioFile);
            if (transcription == null || transcription.isEmpty()) {
                System.out.println("❌ Transcription failed!");
                return;
            }

            // Save transcription to PDF
            saveToPdf(transcription, "src/main/resources/OutputFile/speech.pdf");
            System.out.println("✅ Saved transcription to speech.pdf");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String extractVideoId(String url) {
        return url.split("v=")[1].split("&")[0];
    }

    private static String downloadAudio(String videoId) {
        try {
            String outputDir = "src/main/resources/OutputFile";
            File dir = new File(outputDir);
            if (!dir.exists()) dir.mkdirs();  // Ensure directory exists

            String outputFile = outputDir + "/audio.mp3";
            System.out.println("📥 Downloading audio to: " + outputFile);

            ProcessBuilder pb = new ProcessBuilder(
                "yt-dlp", "--extract-audio", "--audio-format", "mp3",
                "-o", outputFile, "https://www.youtube.com/watch?v=" + videoId
            );
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();

            File file = new File(outputFile);
            if (!file.exists()) {
                System.out.println("❌ Error: Audio file was not saved.");
                return null;
            }

            return outputFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String transcribeAudio(String audioFile) {
        try {
            System.out.println("🎙️ Transcribing audio: " + audioFile);

            ProcessBuilder pb = new ProcessBuilder("python", "scripts/transcribe.py", audioFile);
            pb.redirectErrorStream(true);  // Capture both stdout and stderr
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            StringBuilder transcription = new StringBuilder();
            boolean startCapturing = false; // Flag to detect actual transcription

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Python Output: " + line);

                // Start capturing text only after we detect a certain phrase (adjust as needed)
                if (line.contains("Transcription complete!")) {
                    startCapturing = true;
                    continue;
                }

                if (startCapturing) {
                    transcription.append(line).append("\n"); // Append transcribed text
                }
            }

            process.waitFor();
            return transcription.toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    private static void saveToPdf(String text, String filePath) throws IOException {
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