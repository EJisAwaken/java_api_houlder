#!/bin/bash

echo "Building the application..."
mvn clean package

if [ $? -ne 0 ]; then
    echo "Build failed. Please check the errors above."
    exit 1
fi

echo "Starting the application..."
echo "Press Ctrl+C to stop the server when you're done testing."
echo ""
echo "Once the server is running, open a web browser and navigate to:"
echo "http://localhost:8080/"
echo ""
echo "You should see the Ticket Queue System interface where you can:"
echo "- Create new tickets"
echo "- View the next ticket in the queue"
echo "- Remove tickets from the queue"
echo "- View all tickets in the queue"
echo ""

# Run the application
java -cp target/java-1.0-SNAPSHOT.jar org.example.Main