#!/bin/bash

# Configuration - can be overridden via environment variables
API_KEY="${ELASTIC_API_KEY:-}"
ELASTICSEARCH_URL="${ELASTIC_URL:-http://localhost:9200}"
INDEX_NAME="${ELASTIC_INDEX_NAME:-dummy-test-junit}"

echo "Setting up Elasticsearch templates for ${INDEX_NAME}..."

# Build authorization header if API key is provided
AUTH_HEADER=""
if [ -n "$API_KEY" ]; then
    AUTH_HEADER="-H \"Authorization: ApiKey ${API_KEY}\""
fi

# Step 1: Create Component Template for Settings
echo "Step 1: Creating component template for settings..."
curl -X PUT "${ELASTICSEARCH_URL}/_component_template/${INDEX_NAME}-settings" \
  -H "Content-Type: application/json" \
  ${API_KEY:+-H "Authorization: ApiKey ${API_KEY}"} \
  -d '{
  "template": {
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0,
      "mapping.nested_objects.limit": 100,
      "index.mapping.total_fields.limit": 2000
    }
  }
}'

echo -e "\n"

# Step 2: Create Component Template for Mappings with a Runtime Field
echo "Step 2: Creating component template for mappings..."
curl -X PUT "${ELASTICSEARCH_URL}/_component_template/${INDEX_NAME}-mappings" \
  -H "Content-Type: application/json" \
  ${API_KEY:+-H "Authorization: ApiKey ${API_KEY}"} \
  -d '{
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
                },
                "extra": {
                  "type": "object",
                  "properties": {
                    "startupDuration": {
                      "type": "long"
                    }
                  }
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
            "source": "emit(doc[\"results.summary.stop\"].value.toInstant().toEpochMilli() - doc[\"results.summary.start\"].value.toInstant().toEpochMilli());"
          }
        }
      }
    }
  }
}'

echo -e "\n"

# Step 3: Create Composable Index Template
echo "Step 3: Creating composable index template..."
curl -X PUT "${ELASTICSEARCH_URL}/_index_template/${INDEX_NAME}-template" \
  -H "Content-Type: application/json" \
  ${API_KEY:+-H "Authorization: ApiKey ${API_KEY}"} \
  -d "{
  \"index_patterns\": [\"${INDEX_NAME}*\"],
  \"priority\": 100,
  \"composed_of\": [
    \"${INDEX_NAME}-settings\",
    \"${INDEX_NAME}-mappings\"
  ],
  \"template\": {
    \"settings\": {
      \"mapping.nested_objects.limit\": 100
    }
  }
}"

echo -e "\n"
echo "Elasticsearch templates setup completed!"
