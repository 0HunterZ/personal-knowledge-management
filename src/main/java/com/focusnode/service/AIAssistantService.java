package com.focusnode.service;

import com.focusnode.model.DashboardMetrics;
import com.focusnode.model.UserSettings;
import com.focusnode.repository.UserSettingsRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AIAssistantService {

    private final UserSettingsRepository settingsRepository;
    private final HttpClient httpClient;

    public AIAssistantService() {
        this.settingsRepository = new UserSettingsRepository();
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public CompletableFuture<String> generateProductivityReport(DashboardMetrics metrics) {
        return CompletableFuture.supplyAsync(() -> {
            UserSettings settings = settingsRepository.findByUserId(1);
            if (settings == null || settings.getGeminiApiKey() == null || settings.getGeminiApiKey().isBlank()) {
                throw new IllegalStateException("Gemini API Key is not configured. Please add it in Settings.");
            }

            String apiKey = settings.getGeminiApiKey();
            String prompt = buildPrompt(metrics);
            
            try {
                JsonObject requestBody = new JsonObject();
                JsonArray contents = new JsonArray();
                JsonObject partsObj = new JsonObject();
                JsonArray parts = new JsonArray();
                JsonObject textObj = new JsonObject();
                textObj.addProperty("text", prompt);
                parts.add(textObj);
                partsObj.add("parts", parts);
                contents.add(partsObj);
                requestBody.add("contents", contents);

                JsonObject systemInstruction = new JsonObject();
                JsonObject sysPartsObj = new JsonObject();
                JsonArray sysParts = new JsonArray();
                JsonObject sysTextObj = new JsonObject();
                sysTextObj.addProperty("text", "You are an AI Study Assistant for the Focus-Node app. You analyze user's learning metrics and provide actionable advice. Use markdown format. Be encouraging but practical.");
                sysParts.add(sysTextObj);
                sysPartsObj.add("parts", sysParts);
                systemInstruction.add("system_instruction", sysPartsObj);
                requestBody.add("system_instruction", systemInstruction);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("API Error " + response.statusCode() + ": " + response.body());
                }

                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                if (jsonResponse.has("candidates")) {
                    JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
                    if (candidates.size() > 0) {
                        JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
                        if (content.has("parts")) {
                            JsonArray resParts = content.getAsJsonArray("parts");
                            if (resParts.size() > 0) {
                                return resParts.get(0).getAsJsonObject().get("text").getAsString();
                            }
                        }
                    }
                }
                throw new RuntimeException("Unexpected response format from Gemini API.");
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate AI report: " + e.getMessage(), e);
            }
        });
    }

    private String buildPrompt(DashboardMetrics metrics) {
        StringBuilder sb = new StringBuilder();
        sb.append("Please analyze my productivity data for the past week and provide a short, actionable report with advice to improve my learning.\n\n");
        sb.append("Here is my data:\n");
        sb.append("- Total Focus Time: ").append(metrics.getTotalFocusMinutesThisWeek()).append(" minutes\n");
        sb.append("- Focus Sessions: ").append(metrics.getFocusSessionsThisWeek()).append("\n");
        sb.append("- Task Completion Rate: ").append(String.format("%.0f%%", metrics.getTaskCompletionRateThisWeek() * 100)).append("\n");
        sb.append("- Current Streak: ").append(metrics.getCurrentStreak()).append(" days\n");
        sb.append("- Focus Score (out of 100): ").append(String.format("%.1f", metrics.getFocusScoreThisWeek())).append("\n");
        
        sb.append("- Average Session Length: ").append(metrics.getAvgSessionMinutes()).append(" minutes\n");
        sb.append("- Morning Focus Percent: ").append(String.format("%.0f%%", metrics.getMorningFocusPercent())).append("\n");
        
        sb.append("\nBreakdown by Category:\n");
        for (Map.Entry<String, Integer> entry : metrics.getCategoryFocusMinutesThisWeek().entrySet()) {
            if (entry.getValue() > 0) {
                sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" minutes\n");
            }
        }
        
        sb.append("\nPlease include:\n");
        sb.append("1. A brief summary of my performance.\n");
        sb.append("2. What I did well.\n");
        sb.append("3. Actionable advice on how to improve my focus, scheduling, or category balance.\n");
        return sb.toString();
    }
}
