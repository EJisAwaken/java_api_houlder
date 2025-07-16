#!/bin/bash

# Start the application in the background
echo "Starting the application..."
java -cp classes org.example.Main &
APP_PID=$!

# Wait for the application to start
echo "Waiting for the application to start..."
sleep 5

# Test the counters endpoint first
echo "Testing the counters endpoint..."
echo "Request URL: http://localhost:8080/api/counters"
response_counters=$(curl -v -s -w "%{http_code}" http://localhost:8080/api/counters -o /tmp/response_counters.json 2>&1)
echo "Response: $response_counters"

# Check the response
status_code_counters=$(echo "$response_counters" | grep -o "< HTTP/1.1 [0-9]*" | awk '{print $3}')
echo "Status code: $status_code_counters"

if [[ $status_code_counters == "200" ]]; then
  echo "Test passed: Counters endpoint returned 200 OK"
  cat /tmp/response_counters.json
else
  echo "Test failed: Counters endpoint returned $status_code_counters"
  cat /tmp/response_counters.json
fi

# Test the toggle endpoint
echo "Testing the toggle endpoint..."
echo "Request URL: http://localhost:8080/api/counters/1/toggle"
response=$(curl -v -s -w "%{http_code}" -X POST http://localhost:8080/api/counters/1/toggle -o /tmp/response.json 2>&1)
echo "Response: $response"

# Check the response
status_code=$(echo "$response" | grep -o "< HTTP/1.1 [0-9]*" | awk '{print $3}')
echo "Status code: $status_code"

if [[ $status_code == "200" ]]; then
  echo "Test passed: Toggle endpoint returned 200 OK"
  cat /tmp/response.json
else
  echo "Test failed: Toggle endpoint returned $status_code"
  cat /tmp/response.json
fi

# Kill the application
echo "Stopping the application..."
kill $APP_PID

# Clean up
rm /tmp/response.json
