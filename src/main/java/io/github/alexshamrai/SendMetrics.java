package io.github.alexshamrai;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import io.github.alexshamrai.ctrf.model.CtrfJson;
import io.github.alexshamrai.ctrf.model.Results;
import io.github.alexshamrai.ctrf.model.Summary;

/**
 * A Java application that reads ctrf-report.json and sends metrics to Prometheus.
 * This is a Java implementation of the send_metrics.sh script.
 */
public class SendMetrics {
    private static final String REPORT_PATH = "build/test-results/test/ctrf-report.json";
    private static final String PUSHGATEWAY_URL = "http://localhost:9091/metrics/job/ctrf_tests";

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
            Results results = report.getResults();
            int passed = summary.getPassed();
            int failed = summary.getFailed();
            int skipped = summary.getSkipped();
            long start = summary.getStart();
            long stop = summary.getStop();
            long duration = stop - start;

            // Print metrics to stdout
            System.out.println("ctrf_test_total_passed " + passed);
            System.out.println("ctrf_test_total_failed " + failed);
            System.out.println("ctrf_test_total_skipped " + skipped);
            System.out.println("ctrf_duration " + duration);

            // Define labels
            String runId = report.getResults().getEnvironment().getBuildNumber();
            String buildName =report.getResults().getEnvironment().getBuildName();

            // Send metrics to Prometheus Pushgateway
            sendMetric("ctrf_test_total_passed", passed, runId, buildName);
            sendMetric("ctrf_test_total_failed", failed, runId, buildName);
            sendMetric("ctrf_test_total_skipped", skipped, runId, buildName);
            sendMetric("ctrf_duration", duration, runId, buildName);

            System.out.println("Metrics sent to Prometheus Pushgateway successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void sendMetric(String metricName, Number value, String runId, String buildName) throws IOException {
        String data = String.format("%s{run_id=\"%s\", build_name=\"%s\"} %s\n", 
                                   metricName, runId, buildName, value);

        URL url = new URL(PUSHGATEWAY_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");  // Use POST to add metrics without replacing existing ones
        conn.setDoOutput(true);

        byte[] postData = data.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
        conn.setRequestProperty("Content-Length", String.valueOf(postData.length));

        try (var os = conn.getOutputStream()) {
            os.write(postData);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_ACCEPTED) {
            throw new IOException("Failed to send metric: " + metricName + ", HTTP error code: " + responseCode);
        }

        conn.disconnect();
    }
}
