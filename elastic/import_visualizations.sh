#!/bin/bash

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Configuration
API_KEY="${ELASTIC_API_KEY:-}"
KIBANA_URL="${KIBANA_URL:-http://localhost:5601}"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

import_file() {
    local file="$1"
    local description="$2"

    if [ ! -f "$file" ]; then
        echo -e "${RED}[SKIP]${NC} File not found: $file"
        return 1
    fi

    echo -e "${YELLOW}Importing:${NC} $description"

    local response=$(curl -s -X POST "${KIBANA_URL}/api/saved_objects/_import?overwrite=true" \
        ${API_KEY:+-H "Authorization: ApiKey ${API_KEY}"} \
        -H "kbn-xsrf: true" \
        -F "file=@${file};type=application/ndjson")

    if echo "$response" | grep -q '"success":true'; then
        echo -e "${GREEN}[OK]${NC} Successfully imported $description"
        return 0
    else
        echo -e "${RED}[FAIL]${NC} Failed to import $description"
        echo "Response: $response"
        return 1
    fi
}

echo "=============================================="
echo "  Importing Kibana Visualizations"
echo "=============================================="
echo ""
echo "Kibana URL: ${KIBANA_URL}"
echo ""

# Check if Kibana is reachable
echo "Checking Kibana connection..."
if ! curl -s "${KIBANA_URL}/api/status" > /dev/null 2>&1; then
    echo -e "${RED}Error: Cannot connect to Kibana at ${KIBANA_URL}${NC}"
    echo "Make sure Kibana is running (docker-compose up -d)"
    exit 1
fi
echo -e "${GREEN}Kibana is reachable${NC}"
echo ""

# Import files
SUCCESS=0
FAIL=0

# Import comprehensive dashboard with all visualizations
if import_file "${SCRIPT_DIR}/visualizations/dashboard_export.ndjson" "Test Automation Metrics Dashboard"; then
    ((SUCCESS++))
else
    ((FAIL++))
fi

echo ""
echo "=============================================="
echo "  Summary"
echo "=============================================="
echo -e "Successful imports: ${GREEN}${SUCCESS}${NC}"
echo -e "Failed imports:     ${RED}${FAIL}${NC}"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}Dashboard imported successfully!${NC}"
    echo ""
    echo "View your dashboard:"
    echo "  1. Open ${KIBANA_URL}"
    echo "  2. Go to Analytics > Dashboard"
    echo "  3. Open 'Test Automation Metrics'"
    echo ""
    echo "Dashboard includes:"
    echo "  - Average Suite Duration (line chart)"
    echo "  - Build Success Rate (metric with color coding)"
    echo "  - Test Statistics (stacked bar: passed/failed/skipped)"
    echo "  - Run Duration (bar chart by build number)"
    echo "  - Test Duration Table (Vega)"
    echo "  - Tests Success Rate Table (Vega with color coding)"
    echo "  - Build Name filter control"
else
    echo -e "${YELLOW}Import failed. Check the errors above.${NC}"
    exit 1
fi
