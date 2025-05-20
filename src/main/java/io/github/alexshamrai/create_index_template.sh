curl -X PUT "http://localhost:9200/_template/ad-serving-automation_template" -H "Content-Type: application/json" -d '{
  "index_patterns": ["ad-serving-automation*"],
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0,
    "mapping.nested_objects.limit": 50,
    "index.mapping.total_fields.limit": 2000
  },
  "mappings": {
    "dynamic": "strict",
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
              },
              "duration": {"type": "integer"}
            }
          },
          "tests": {
            "type": "nested",
            "include_in_parent": true,
            "dynamic": "strict",
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
              "package": {"type": "keyword"}
            }
          }
        }
      }
    }
  }
}'