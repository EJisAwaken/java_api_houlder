#!/bin/bash

# Build script for the Ticket Queue API

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Building Ticket Queue API...${NC}"

# Create classes directory if it doesn't exist
mkdir -p classes

# Compile Java files
echo -e "${BLUE}Compiling Java files...${NC}"
javac -d classes $(find src/main/java -name "*.java")

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Compilation successful!${NC}"

    # Copy resources
    echo -e "${BLUE}Copying resources...${NC}"
    mkdir -p classes/static
    cp -r src/main/resources/static/* classes/static/

    # Run the application
    echo -e "${BLUE}Starting the server...${NC}"
    echo -e "${BLUE}API will be available at http://localhost:8080${NC}"
    java -cp classes org.example.Main
else
    echo -e "\033[0;31mCompilation failed!${NC}"
    exit 1
fi
