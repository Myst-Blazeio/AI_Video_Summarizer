package com.project.aisummarizer.LectureSummarizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONObject;

public class GoogleAISummarizer {

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent";
    private static final String API_KEY = "AIzaSyDnKJooQulG14jluMBTg0CeO4OBvZrz5tU"; // 🔑 Replace with your actual API
                                                                                     // key

    public String generateSummary(String inputText) {
        try {
            String apiUrlWithKey = API_URL + "?key=" + API_KEY;
            URL url = URI.create(apiUrlWithKey).toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject requestBody = new JSONObject();
            org.json.JSONArray contents = new org.json.JSONArray();
            JSONObject part = new JSONObject();

            // 🌟 Highlighted Prompt Area: Summarize in 60 words or less 🌟
            String prompt = "Summarize this text in 60 words or less:\n\n" + inputText;
            part.put("text", prompt);
            // 🌟 End Highlighted Area 🌟

            JSONObject content = new JSONObject();
            content.put("parts", new org.json.JSONArray().put(part));
            contents.put(content);
            requestBody.put("contents", contents);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                try (Scanner scanner = new Scanner(conn.getInputStream(), "utf-8")) {
                    String response = scanner.useDelimiter("\\A").next();
                    JSONObject jsonResponse = new JSONObject(response);

                    if (!jsonResponse.has("candidates")) {
                        return "Error: 'candidates' key missing in response.";
                    }

                    org.json.JSONArray candidates = jsonResponse.getJSONArray("candidates");
                    if (candidates.length() == 0) {
                        return "Error: No summary generated.";
                    }

                    JSONObject candidate = candidates.getJSONObject(0);
                    if (!candidate.has("content")) {
                        return "Error: 'content' key missing in candidate.";
                    }
                    JSONObject contentResponse = candidate.getJSONObject("content");
                    if (!contentResponse.has("parts")) {
                        return "Error: 'parts' key missing in content.";
                    }
                    org.json.JSONArray parts = contentResponse.getJSONArray("parts");
                    if (parts.length() == 0) {
                        return "Error: No parts in content.";
                    }

                    return parts.getJSONObject(0).getString("text");
                }
            } else {
                String errorMessage = getErrorStreamAsString(conn.getErrorStream());
                return "Error generating summary. HTTP Status Code: " + responseCode + ". Error Message: "
                        + errorMessage;
            }

        } catch (Exception e) {
            return "Error generating summary: " + e.getMessage(); // Simplified error message
        }
    }

    private static String getErrorStreamAsString(InputStream errorStream) throws IOException {
        if (errorStream == null) {
            return "No error message provided.";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, "utf-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
