package com.prism.core.provider.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Thin HTTP client for Ollama running locally at localhost:11434.
 * Uses Java 11 HttpClient — no extra dependencies needed.
 */
@Slf4j
@Component
public class OllamaClient {

    private final String baseUrl;
    private final String model;
    private final Duration timeout;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OllamaClient(
            @Value("${prism.llm.ollama-base-url:http://localhost:11434}") String baseUrl,
            @Value("${prism.llm.ollama-model:llama3.1:8b}") String model,
            @Value("${prism.llm.timeout-seconds:120}") long timeoutSeconds,
            ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Send a prompt to Ollama and return the full response text.
     * Uses /api/generate with stream=false so the response arrives all at once.
     */
    public String generate(String prompt) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            body.put("prompt", prompt);
            body.put("stream", false);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .timeout(timeout)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            log.info("[OLLAMA] Sending prompt to {} model={}", baseUrl, model);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Ollama returned HTTP " + response.statusCode() + ": " + response.body());
            }

            JsonNode json = objectMapper.readTree(response.body());
            String text = json.path("response").asText();
            log.info("[OLLAMA] Received response ({} chars)", text.length());
            return text;

        } catch (Exception e) {
            log.error("[OLLAMA] Failed to call Ollama: {}", e.getMessage(), e);
            throw new RuntimeException("Ollama call failed: " + e.getMessage(), e);
        }
    }
}
