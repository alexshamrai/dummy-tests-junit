#!/bin/bash

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

# Configuration - can be overridden via environment variables
ELASTICSEARCH_URL="${ELASTIC_URL:-http://localhost:9200}"
API_KEY="${ELASTIC_API_KEY:-}"

# Index names for different test suites
ACCEPTANCE_INDEX="${ELASTIC_INDEX_NAME:-dummy-test-junit-acceptance}"
REGRESSION_INDEX="${ELASTIC_INDEX_NAME:-dummy-test-junit-regression}"

# Resource directories
SAMPLE_DATA_DIR="${PROJECT_DIR}/src/main/resources/sampleData"
ACCEPTANCE_DIR="${SAMPLE_DATA_DIR}/acceptance"
REGRESSION_DIR="${SAMPLE_DATA_DIR}/regression"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counter for statistics
SUCCESS_COUNT=0
FAIL_COUNT=0

send_to_elasticsearch() {
    local file="$1"
    local index="$2"
    local date_suffix="$3"

    local full_index="${index}-${date_suffix}"
    local url="${ELASTICSEARCH_URL}/${full_index}/_doc"

    # Build curl command
    local curl_cmd="curl -s -X POST \"${url}\" -H \"Content-Type: application/json; charset=UTF-8\""

    if [ -n "$API_KEY" ]; then
        curl_cmd="${curl_cmd} -H \"Authorization: ApiKey ${API_KEY}\""
    fi

    curl_cmd="${curl_cmd} -d @\"${file}\""

    # Execute and capture response
    local response=$(eval $curl_cmd)
    local status=$(echo "$response" | grep -o '"result":"[^"]*"' | cut -d'"' -f4)

    if [ "$status" = "created" ]; then
        echo -e "${GREEN}[OK]${NC} Sent $(basename "$file") -> ${full_index}"
        ((SUCCESS_COUNT++))
    else
        echo -e "${RED}[FAIL]${NC} Failed to send $(basename "$file"): $response"
        ((FAIL_COUNT++))
    fi
}

# Generate date suffixes for the past days to spread data across multiple indices
generate_date() {
    local days_ago=$1
    if [[ "$OSTYPE" == "darwin"* ]]; then
        date -v-${days_ago}d +%Y-%m-%d
    else
        date -d "${days_ago} days ago" +%Y-%m-%d
    fi
}

echo "=============================================="
echo "  Populating Elasticsearch with sample data"
echo "=============================================="
echo ""
echo "Elasticsearch URL: ${ELASTICSEARCH_URL}"
echo "Acceptance index:  ${ACCEPTANCE_INDEX}"
echo "Regression index:  ${REGRESSION_INDEX}"
echo ""

# Check if Elasticsearch is reachable
echo "Checking Elasticsearch connection..."
if ! curl -s "${ELASTICSEARCH_URL}" > /dev/null 2>&1; then
    echo -e "${RED}Error: Cannot connect to Elasticsearch at ${ELASTICSEARCH_URL}${NC}"
    echo "Make sure Elasticsearch is running (docker-compose up -d)"
    exit 1
fi
echo -e "${GREEN}Elasticsearch is reachable${NC}"
echo ""

# Send acceptance test reports (spread across last 10 days)
echo -e "${YELLOW}Sending Acceptance test reports...${NC}"
if [ -d "$ACCEPTANCE_DIR" ]; then
    day_counter=0
    for file in "$ACCEPTANCE_DIR"/ctrf-rep*.json; do
        if [ -f "$file" ]; then
            date_suffix=$(generate_date $day_counter)
            send_to_elasticsearch "$file" "$ACCEPTANCE_INDEX" "$date_suffix"
            ((day_counter++))
            # Reset counter to cycle through dates
            if [ $day_counter -ge 10 ]; then
                day_counter=0
            fi
        fi
    done
else
    echo -e "${RED}Acceptance directory not found: ${ACCEPTANCE_DIR}${NC}"
fi

echo ""

# Send regression test reports (spread across last 10 days)
echo -e "${YELLOW}Sending Regression test reports...${NC}"
if [ -d "$REGRESSION_DIR" ]; then
    day_counter=0
    for file in "$REGRESSION_DIR"/ctrf-rep*.json; do
        if [ -f "$file" ]; then
            date_suffix=$(generate_date $day_counter)
            send_to_elasticsearch "$file" "$REGRESSION_INDEX" "$date_suffix"
            ((day_counter++))
            # Reset counter to cycle through dates
            if [ $day_counter -ge 10 ]; then
                day_counter=0
            fi
        fi
    done
else
    echo -e "${RED}Regression directory not found: ${REGRESSION_DIR}${NC}"
fi

echo ""
echo "=============================================="
echo "  Summary"
echo "=============================================="
echo -e "Successfully sent: ${GREEN}${SUCCESS_COUNT}${NC} reports"
echo -e "Failed:            ${RED}${FAIL_COUNT}${NC} reports"
echo ""

if [ $FAIL_COUNT -eq 0 ]; then
    echo -e "${GREEN}All reports sent successfully!${NC}"
    echo ""
    echo "View your data in Kibana:"
    echo "  1. Open http://localhost:5601"
    echo "  2. Go to Analytics > Discover"
    echo "  3. Create a data view for 'dummy-test-junit-*' to see all data"
else
    echo -e "${YELLOW}Some reports failed to send. Check the errors above.${NC}"
    exit 1
fi
