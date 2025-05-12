#!/bin/bash

REPORT_PATH="build/test-results/test/ctrf-report.json"

if [ ! -f "$REPORT_PATH" ]; then
    echo "Report file not found: $REPORT_PATH"
    exit 1
fi

# Extract metrics from the JSON report
PASSED=11
FAILED=30
SKIPPED=1
DURATION=300

echo "test_total_passed $PASSED"
echo "test_total_failed $FAILED"
echo "test_total_skipped $SKIPPED"
echo "duration $DURATION"
RUN_ID=03
BUILD_NAME=regression

# Push metrics to Pushgateway
echo "test_total_passed{run_id=\"$RUN_ID\", build_name=\"$BUILD_NAME\"} $PASSED" | curl --data-binary @- http://localhost:9091/metrics/job/ctrf_tests
echo "test_total_failed{run_id=\"$RUN_ID\", build_name=\"$BUILD_NAME\"} $FAILED" | curl --data-binary @- http://localhost:9091/metrics/job/ctrf_tests
echo "test_total_skipped{run_id=\"$RUN_ID\", build_name=\"$BUILD_NAME\"} $SKIPPED" | curl --data-binary @- http://localhost:9091/metrics/job/ctrf_tests
echo "duration{run_id=\"$RUN_ID\", build_name=\"$BUILD_NAME\"} $DURATION" | curl --data-binary @- http://localhost:9091/metrics/job/ctrf_tests