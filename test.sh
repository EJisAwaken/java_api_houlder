#!/bin/bash

# Test script for the Ticket Queue API
# This script tests all the API endpoints using curl

# Set the API base URL
API_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print success message
success() {
  echo -e "${GREEN}✓ $1${NC}"
}

# Function to print error message
error() {
  echo -e "${RED}✗ $1${NC}"
  exit 1
}

echo "Starting API tests..."

# Test 1: Get API information
echo "Test 1: Get API information"
response=$(curl -s $API_URL/)
if [[ $response == *"Ticket Queue API"* ]]; then
  success "Root endpoint returns API information"
else
  error "Root endpoint test failed"
fi

# Test 2: Get all tickets (should be empty initially)
echo "Test 2: Get all tickets (empty queue)"
response=$(curl -s $API_URL/api/tickets)
if [[ $response == "[]" ]]; then
  success "Empty queue returns empty array"
else
  error "Empty queue test failed"
fi

# Test 3: Try to peek at next ticket (should fail)
echo "Test 3: Peek at next ticket (empty queue)"
response=$(curl -s -w "%{http_code}" $API_URL/api/tickets/next -o /dev/null)
if [[ $response == "404" ]]; then
  success "Peek at empty queue returns 404"
else
  error "Peek at empty queue test failed"
fi

# Test 4: Try to remove ticket (should fail)
echo "Test 4: Remove ticket (empty queue)"
response=$(curl -s -w "%{http_code}" -X DELETE $API_URL/api/tickets -o /dev/null)
if [[ $response == "404" ]]; then
  success "Remove from empty queue returns 404"
else
  error "Remove from empty queue test failed"
fi

# Test 5: Create a new ticket
echo "Test 5: Create a new ticket"
response=$(curl -s -X POST -d "Test ticket 1" $API_URL/api/tickets)
if [[ $response == *"Test ticket 1"* ]]; then
  success "Ticket created successfully"
else
  error "Ticket creation failed"
fi

# Test 6: Get all tickets (should have one ticket now)
echo "Test 6: Get all tickets (one ticket)"
response=$(curl -s $API_URL/api/tickets)
if [[ $response == *"Test ticket 1"* ]]; then
  success "Queue has one ticket"
else
  error "Get all tickets test failed"
fi

# Test 7: Peek at next ticket
echo "Test 7: Peek at next ticket"
response=$(curl -s $API_URL/api/tickets/next)
if [[ $response == *"Test ticket 1"* ]]; then
  success "Peek returns the correct ticket"
else
  error "Peek test failed"
fi

# Test 8: Create another ticket
echo "Test 8: Create another ticket"
response=$(curl -s -X POST -d "Test ticket 2" $API_URL/api/tickets)
if [[ $response == *"Test ticket 2"* ]]; then
  success "Second ticket created successfully"
else
  error "Second ticket creation failed"
fi

# Test 9: Get all tickets (should have two tickets now)
echo "Test 9: Get all tickets (two tickets)"
response=$(curl -s $API_URL/api/tickets)
if [[ $response == *"Test ticket 1"* && $response == *"Test ticket 2"* ]]; then
  success "Queue has two tickets"
else
  error "Get all tickets test failed"
fi

# Test 10: Remove a ticket (should remove the first one)
echo "Test 10: Remove a ticket"
response=$(curl -s -X DELETE $API_URL/api/tickets)
if [[ $response == *"Test ticket 1"* ]]; then
  success "First ticket removed successfully"
else
  error "Ticket removal failed"
fi

# Test 11: Peek at next ticket (should be the second one now)
echo "Test 11: Peek at next ticket after removal"
response=$(curl -s $API_URL/api/tickets/next)
if [[ $response == *"Test ticket 2"* ]]; then
  success "Peek returns the second ticket"
else
  error "Peek after removal test failed"
fi

# Test 12: Get all tickets (should have one ticket left)
echo "Test 12: Get all tickets after removal"
response=$(curl -s $API_URL/api/tickets)
if [[ $response == *"Test ticket 2"* && $response != *"Test ticket 1"* ]]; then
  success "Queue has only the second ticket"
else
  error "Get all tickets after removal test failed"
fi

# Test 13: Remove the last ticket
echo "Test 13: Remove the last ticket"
response=$(curl -s -X DELETE $API_URL/api/tickets)
if [[ $response == *"Test ticket 2"* ]]; then
  success "Last ticket removed successfully"
else
  error "Last ticket removal failed"
fi

# Test 14: Get all tickets (should be empty again)
echo "Test 14: Get all tickets (empty queue again)"
response=$(curl -s $API_URL/api/tickets)
if [[ $response == "[]" ]]; then
  success "Queue is empty again"
else
  error "Empty queue after removals test failed"
fi

echo "All tests passed successfully!"