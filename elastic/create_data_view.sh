#!/bin/bash

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Configuration - can be overridden via environment variables
API_KEY="${ELASTIC_API_KEY:-}"
KIBANA_URL="${KIBANA_URL:-http://localhost:5601}"
INDEX_NAME="${ELASTIC_INDEX_NAME:-dummy-test-junit}"
DATA_VIEW_FILE="${1:-${SCRIPT_DIR}/data_view_export.ndjson}"

# Check if the data view file exists
if [ ! -f "$DATA_VIEW_FILE" ]; then
    echo "Error: Data view file '$DATA_VIEW_FILE' not found!"
    echo "Please create the file with the required content first."
    exit 1
fi

echo "Importing data view to Kibana..."

# Import Data View using multipart form data
curl -X POST "${KIBANA_URL}/api/saved_objects/_import?overwrite=true" \
  ${API_KEY:+-H "Authorization: ApiKey ${API_KEY}"} \
  -H "kbn-xsrf: true" \
  -F "file=@${DATA_VIEW_FILE};type=application/ndjson;filename=data_view_export.ndjson"

echo -e "\n"
echo "Data view import completed!"
