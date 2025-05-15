package io.github.alexshamrai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
            int passed = summary.getPassed();
            int failed = summary.getFailed();
            int skipped = summary.getSkipped();
            long start = summary.getStart();
            long stop = summary.getStop();
            long duration = stop - start;

            // Print metrics to stdout
            System.out.println("test_total_passed " + passed);
            System.out.println("test_total_failed " + failed);
            System.out.println("test_total_skipped " + skipped);
            System.out.println("duration " + duration);

            // Define labels
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

    private static void sendToElasticsearch(Summary summary) throws IOException {
        // Create JSON payload for Elasticsearch
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(summary);

        // Send data to Elasticsearch
        URL url = new URL(ELASTICSEARCH_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (var os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
            throw new IOException("Failed to send test summary to Elasticsearch, HTTP error code: " + responseCode);
        }

        conn.disconnect();
    }
}