# Test Automation Metrics in Elasticsearch and Kibana

This article explains how to set up test automation metrics collection in Elasticsearch and Kibana for any Java Gradle project that uses CTRF (Common Test Report Format) reporting. By following these steps, you'll be able to:

1. Send CTRF test reports to Elasticsearch
2. Set up Elasticsearch with the proper index templates
3. Configure Kibana dashboards for visualizing test metrics

## Prerequisites

- A Java Gradle project with CTRF reporting enabled
- Access to an Elasticsearch cluster
- Access to Kibana
- Basic knowledge of Gradle, Elasticsearch, and Kibana

## 1. How to Send CTRF Reports to Elasticsearch

### Step 1: Add the Gradle Script

Create a file named `send-metrics-to-elastic.gradle` in your project's `gradle` directory with the following content:

```groovy
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.nio.charset.StandardCharsets

tasks.register('sendMetricsToElastic') {
    description = 'Sends test metrics to Elasticsearch'
    ext.reportPath = project.findProperty('reportPath') ?: 'build/test-results/test/ctrf-report.json'

    doLast {
        final int CONNECTION_TIMEOUT_SECONDS = 10

        try {
            def reportFile = validateReportFile(reportPath)
            def config = getElasticSearchConfig()
            def requestData = prepareRequestData(reportFile, config)
            sendDataToElasticsearch(requestData, CONNECTION_TIMEOUT_SECONDS)

        } catch (Exception e) {
            handleException("Failed to send metrics to Elasticsearch", e)
        }
    }
}

private File validateReportFile(String reportFilePath) {
    def reportFile = file(reportFilePath)
    if (!reportFile.exists()) {
        def errorMsg = "CTRF report file not found at: ${reportFile.absolutePath}"
        println errorMsg
        throw new GradleException(errorMsg)
    }
    return reportFile
}

private Map getElasticSearchConfig() {
    return [
        elasticUrl: System.getenv('ELASTIC_URL') ?: 'http://localhost:9200',
        apiKey: System.getenv('ELASTIC_API_KEY') ?: '',
        indexName: System.getenv('ELASTIC_INDEX_NAME') ?: 'test-automation'
    ]
}

private Map prepareRequestData(File reportFile, Map config) {
    def formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    def fullUrl = "${config.elasticUrl}/${config.indexName}-${formattedDate}/_doc"
    def jsonPayload = reportFile.text

    return [
        url: fullUrl,
        payload: jsonPayload,
        apiKey: config.apiKey
    ]
}

private void sendDataToElasticsearch(Map requestData, int timeoutSeconds) {
    def client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeoutSeconds))
            .build()

    def requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(requestData.url))
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(requestData.payload, StandardCharsets.UTF_8))

    if (requestData.apiKey) {
        requestBuilder.header("Authorization", "ApiKey ${requestData.apiKey}")
    }

    def request = requestBuilder.build()
    def response = client.send(request, HttpResponse.BodyHandlers.ofString())

    handleResponse(response, requestData.url)
}

private void handleResponse(HttpResponse<String> response, String url) {
    def statusCode = response.statusCode()
    if (statusCode == 200 || statusCode == 201) {
        println "Successfully sent test metrics to Elasticsearch at ${url}"
    } else {
        def errorMsg = "Failed to send metrics to Elasticsearch, HTTP error code: ${statusCode}. Response: ${response.body()}"
        println errorMsg
        throw new GradleException(errorMsg)
    }
}

private void handleException(String message, Exception exception) {
    def errorMsg = "${message}: ${exception.message}"
    println errorMsg
    exception.printStackTrace()
    throw new GradleException(errorMsg)
}
```

### Step 2: Apply the Script in Your Main build.gradle File

Add the following line to your main `build.gradle` file:

```groovy
apply from: 'gradle/send-metrics-to-elastic.gradle'
```

### Step 3: Configure Environment Variables

The script uses the following environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `ELASTIC_URL` | Elasticsearch server URL | `http://localhost:9200` |
| `ELASTIC_API_KEY` | API key for authentication | (empty) |
| `ELASTIC_INDEX_NAME` | Base name for the Elasticsearch index | `test-automation` |

You can set these variables in your CI/CD environment or locally for testing.

### Step 4: Run the Task After Tests

You can run the task manually after tests complete:

```bash
./gradlew test
./gradlew sendMetricsToElastic
```

Or you can configure it to run automatically after tests by adding this to your `build.gradle`:

```groovy
test.finalizedBy(sendMetricsToElastic)
```

## 2. How to Set Up Elasticsearch

### Step 1: Create Index Templates

Elasticsearch needs to be configured with the proper index templates to correctly store and query the test metrics data. Create a file named `CreateTemplate.http` with the following content:

```http
@apiKey = your_api_key_here

### Step 1: Create Component Template for Settings
PUT http://localhost:9200/_component_template/test-automation-settings
Content-Type: application/json
Authorization: ApiKey {{apiKey}}

{
  "template": {
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0,
      "mapping.nested_objects.limit": 100,
      "index.mapping.total_fields.limit": 2000
    }
  }
}

### Step 2: Create Component Template for Mappings with a Runtime Field
PUT http://localhost:9200/_component_template/test-automation-mappings
Content-Type: application/json
Authorization: ApiKey {{apiKey}}

{
  "template": {
    "mappings": {
      "dynamic": true,
      "date_detection": false,
      "properties": {
        "reportFormat": {"type": "keyword"},
        "specVersion": {"type": "keyword"},
        "results": {
          "type": "object",
          "properties": {
            "tool": {
              "type": "object",
              "properties": {
                "name": {"type": "keyword"},
                "version": {"type": "keyword"}
              }
            },
            "summary": {
              "type": "object",
              "properties": {
                "tests": {"type": "integer"},
                "passed": {"type": "integer"},
                "failed": {"type": "integer"},
                "pending": {"type": "integer"},
                "skipped": {"type": "integer"},
                "other": {"type": "integer"},
                "start": {
                  "type": "date",
                  "format": "epoch_millis"
                },
                "stop": {
                  "type": "date",
                  "format": "epoch_millis"
                }
              }
            },
            "tests": {
              "type": "nested",
              "include_in_parent": true,
              "properties": {
                "name": {"type": "keyword"},
                "status": {"type": "keyword"},
                "duration": {"type": "integer"},
                "start": {
                  "type": "date",
                  "format": "epoch_millis"
                },
                "stop": {
                  "type": "date",
                  "format": "epoch_millis"
                },
                "message": {
                  "type": "text",
                  "fields": {
                    "keyword": {"type": "keyword", "ignore_above": 256}
                  }
                },
                "trace": {
                  "type": "text",
                  "fields": {
                    "keyword": {"type": "keyword", "ignore_above": 256}
                  }
                },
                "tags": {"type": "keyword"},
                "filepath": {"type": "keyword"},
                "threadId": {"type": "keyword"},
                "classname": {"type": "keyword"},
                "package": {"type": "keyword"},
                "retries": {"type": "integer"},
                "flaky": {"type": "boolean"}
              }
            },
            "environment": {
              "type": "object",
              "properties": {
                "buildName": {"type": "keyword"},
                "buildNumber": {"type": "keyword"},
                "buildUrl": {"type": "keyword"},
                "commit": {"type": "keyword"},
                "branchName": {"type": "keyword"}
              }
            }
          }
        }
      },
      "runtime": {
        "results.summary.duration": {
          "type": "long",
          "script": {
            "source": "emit(doc['results.summary.stop'].value.toInstant().toEpochMilli() - doc['results.summary.start'].value.toInstant().toEpochMilli());"
          }
        }
      }
    }
  }
}

### Step 3: Create Composable Index Template
PUT http://localhost:9200/_index_template/test-automation-template
Content-Type: application/json
Authorization: ApiKey {{apiKey}}

{
  "index_patterns": ["test-automation*"],
  "priority": 100,
  "composed_of": [
    "test-automation-settings",
    "test-automation-mappings"
  ],
  "template": {
    "settings": {
      "mapping.nested_objects.limit": 100
    }
  }
}
```

You can execute these requests using any HTTP client that supports .http files, such as:
- IntelliJ IDEA
- VS Code with the REST Client extension
- curl (by extracting the requests)

### Step 2: Verify the Templates

After creating the templates, you can verify they were created correctly by running:

```http
GET http://localhost:9200/_component_template/test-automation-settings
GET http://localhost:9200/_component_template/test-automation-mappings
GET http://localhost:9200/_index_template/test-automation-template
```

## 3. How to Set Up Kibana

### Step 1: Create a Data View

First, you need to create a data view in Kibana to visualize the test metrics data. You can do this manually through the Kibana UI or by importing a pre-configured data view.

To import a pre-configured data view, create a file named `RestoreDataView.http` with the following content:

```http
@apiKey = your_api_key_here
@namespace = test-automation

### Import Data View
POST http://localhost:5601/s/{{namespace}}/api/saved_objects/_import?overwrite=true
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW
Authorization: ApiKey {{apiKey}}
kbn-xsrf: true

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename="data_view_export.ndjson"
Content-Type: application/ndjson

< ./data_view_export.ndjson
------WebKitFormBoundary7MA4YWxkTrZu0gW--
```

You'll need to create a `data_view_export.ndjson` file with the following content:

```json
{"attributes":{"fieldAttrs":"{}","fieldFormatMap":"{}","fields":"[]","name":"test-automation","namespaceType":"single","runtimeFieldMap":"{}","sourceFilters":"[]","timeFieldName":"results.summary.start","title":"test-automation-*","typeMeta":"{}"},"coreMigrationVersion":"8.11.1","id":"test-automation","managed":false,"references":[],"type":"index-pattern","updated_at":"2023-06-15T12:00:00.000Z","version":"WzEyMzQ1Njc4OTBd"}
```

### Step 2: Create a Dashboard

Next, you need to create a dashboard to visualize the test metrics. You can do this manually through the Kibana UI or by importing a pre-configured dashboard.

To import a pre-configured dashboard, create a file named `RestoreDashboard.http` with the following content:

```http
@apiKey = your_api_key_here
@namespace = test-automation

### Import Dashboard
POST http://localhost:5601/s/{{namespace}}/api/saved_objects/_import?overwrite=true
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW
Authorization: ApiKey {{apiKey}}
kbn-xsrf: true

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename="dashboard_export.ndjson"
Content-Type: application/ndjson

< ./dashboard_export.ndjson
------WebKitFormBoundary7MA4YWxkTrZu0gW--
```

For the `dashboard_export.ndjson` file, you'll need to create a dashboard in Kibana first and then export it. A basic dashboard should include:

1. Test execution summary (pass/fail counts)
2. Test duration trends
3. Failure rate by test class
4. Recent test failures
5. Test execution timeline

## Conclusion

By following these steps, you can set up test automation metrics collection in Elasticsearch and Kibana for any Java Gradle project that uses CTRF reporting. This setup provides:

- Long-term storage of test results
- Historical test performance tracking
- Visualization of test metrics
- Ability to identify flaky tests and performance trends

The configuration is flexible and can be adapted to different project needs by modifying the index templates, data views, and dashboards.