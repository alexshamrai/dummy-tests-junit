#!/bin/bash

# Configuration - can be overridden via environment variables
ELASTICSEARCH_URL="${ELASTIC_URL:-http://localhost:9200}"
INDEX_NAME="${ELASTIC_INDEX_NAME:-dummy-test-junit}"

echo "Verifying Elasticsearch templates for ${INDEX_NAME}..."

# Get component template for settings
echo "Checking component template for settings..."
curl -s -X GET "${ELASTICSEARCH_URL}/_component_template/${INDEX_NAME}-settings" \
  -H "Content-Type: application/json" | python3 -m json.tool 2>/dev/null || cat

echo -e "\n"

# Get component template for mappings
echo "Checking component template for mappings..."
curl -s -X GET "${ELASTICSEARCH_URL}/_component_template/${INDEX_NAME}-mappings" \
  -H "Content-Type: application/json" | python3 -m json.tool 2>/dev/null || cat

echo -e "\n"

# Get index template
echo "Checking index template..."
curl -s -X GET "${ELASTICSEARCH_URL}/_index_template/${INDEX_NAME}-template" \
  -H "Content-Type: application/json" | python3 -m json.tool 2>/dev/null || cat

echo -e "\n"
echo "Template verification completed!"
