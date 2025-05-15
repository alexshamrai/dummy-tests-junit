package io.github.alexshamrai;

import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import io.github.alexshamrai.ctrf.model.CtrfJson;
import io.github.alexshamrai.ctrf.model.Environment;
import io.github.alexshamrai.ctrf.model.Summary;

public class SendMetrics {
    private static final String REPORT_PATH = "build/test-results/test/ctrf-report.json";
    private static final String ELASTICSEARCH_URL = "http://localhost:9200/test_results-" +
                                                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                                                    "/_doc";

    public static void main(String[] args) {
        try {
            File reportFile = new File(REPORT_PATH);
            if (!reportFile.exists()) {
                System.err.println("Report file not found: " + REPORT_PATH);
                System.exit(1);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            CtrfJson report = objectMapper.readValue(reportFile, CtrfJson.class);

            sendToElasticsearch(report);

            System.out.println("Test report sent to Elasticsearch successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void sendToElasticsearch(Summary summary) throws IOException, InterruptedException {
        sendJsonToElasticsearch(summary);
    }

    private static void sendToElasticsearch(CtrfJson report) throws IOException, InterruptedException {
        sendJsonToElasticsearch(report);
    }

    private static void sendToElasticsearch(Environment environment) throws IOException, InterruptedException {
        sendJsonToElasticsearch(environment);
    }

    private static void sendJsonToElasticsearch(Object data) throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(data);

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
            throw new IOException("Failed to send data to Elasticsearch, HTTP error code: " + statusCode + response.body());
        }
    }
}
