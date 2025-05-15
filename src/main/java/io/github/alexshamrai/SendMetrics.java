package io.github.alexshamrai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import io.github.alexshamrai.ctrf.model.CtrfJson;
import io.github.alexshamrai.ctrf.model.Summary;

public class SendMetrics {
    private static final String REPORT_PATH = "build/test-results/test/ctrf-report.json";
    private static final String ELASTICSEARCH_URL = "http://localhost:9200/test_results/_doc";

    public static void main(String[] args) {
        try {
            // Read the report file
            File reportFile = new File(REPORT_PATH);
            if (!reportFile.exists()) {
                System.err.println("Report file not found: " + REPORT_PATH);
                System.exit(1);
            }

            // Parse the JSON file using Jackson
            ObjectMapper objectMapper = new ObjectMapper();
            CtrfJson report = objectMapper.readValue(reportFile, CtrfJson.class);

            // Extract metrics from the parsed report
            Summary summary = report.getResults().getSummary();

            // Define labels (not currently used but kept for future reference)
            String runId = report.getResults().getEnvironment().getBuildNumber();
            String buildName = report.getResults().getEnvironment().getBuildName();

            // Send test summary to Elasticsearch
            sendToElasticsearch(summary);

            System.out.println("Test summary sent to Elasticsearch successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void sendToElasticsearch(Summary summary) throws IOException, InterruptedException {
        // Create JSON payload for Elasticsearch
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(summary);

        // Create HTTP client
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Create HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ELASTICSEARCH_URL))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        // Send request and get response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check response status
        int statusCode = response.statusCode();
        if (statusCode != 200 && statusCode != 201) {
            throw new IOException("Failed to send test summary to Elasticsearch, HTTP error code: " + statusCode);
        }
    }
}
