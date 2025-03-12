package com.project.AIStudio;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import org.json.JSONArray;
import org.json.JSONObject;

public class GoogleAISummarizer {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent";
    private static String API_KEY = "";

    static {
        try {
            API_KEY = loadApiKey();
        } catch (IOException e) {
            System.err.println("⚠️ Failed to load API key: " + e.getMessage());
        }
    }

    private static String loadApiKey() throws IOException {
        Properties properties = new Properties();
        File file = new File(".env");
        if (!file.exists()) {
            throw new IOException(".env file not found.");
        }
        try (InputStream input = new FileInputStream(file)) {
            properties.load(input);
            return properties.getProperty("GOOGLE_API_KEY");
        }
    }

    public String generateSummary(String inputText) {
        if (API_KEY.isEmpty()) {
            return "Error: API Key is missing!";
        }
        try {
            String apiUrlWithKey = API_URL + "?key=" + API_KEY;
            URL url = URI.create(apiUrlWithKey).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", "Summarize this text in 60 words or less:\n\n" + inputText + "end the summary with the word 'Eureka' in the end");
            JSONObject content = new JSONObject();
            content.put("parts", new JSONArray().put(part));
            contents.put(content);
            requestBody.put("contents", contents);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray candidates = jsonResponse.optJSONArray("candidates");

            if (candidates != null && candidates.length() > 0) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject contentResponse = candidate.optJSONObject("content");
                JSONArray parts = contentResponse.optJSONArray("parts");

                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).getString("text");
                }
            }
            return "Error: No summary generated.";
        } catch (Exception e) {
            return "Error generating summary: " + e.getMessage();
        }
    }
}
