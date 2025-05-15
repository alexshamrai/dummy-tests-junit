curl -X PUT "http://localhost:9200/_template/test_results_template" -H "Content-Type: application/json" -d '{
  "index_patterns": ["test_results*"],
  "mappings": {
    "properties": {
      "reportFormat": {"type": "keyword"},
      "specVersion": {"type": "keyword"},
      "results": {
        "type": "object",
        "properties": {
          "tool": {
            "type": "object",
            "properties": {
              "name": {"type": "keyword"}
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
              "start": {"type": "long"},
              "stop": {"type": "long"}
            }
          },
          "tests": {
            "type": "nested",
            "properties": {
              "name": {"type": "keyword"},
              "status": {"type": "keyword"},
              "duration": {"type": "integer"},
              "start": {"type": "long"},
              "stop": {"type": "long"},
              "message": {"type": "text"},
              "trace": {"type": "text"},
              "tags": {"type": "keyword"},
              "filepath": {"type": "keyword"},
              "threadId": {"type": "keyword"}
            }
          }
        }
      }
    }
  }
}'