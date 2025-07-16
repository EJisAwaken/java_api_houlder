# Ticket Queue API

A simple REST API for managing a ticket queue (FIFO) system, built with pure Java (no frameworks).

## Features

- FIFO queue implementation with standard operations (enqueue, dequeue, peek, isEmpty, size)
- REST API endpoints to manipulate the queue
- User-friendly HTML interface for interacting with the queue
- In-memory data storage
- Dockerized application
- Comprehensive test script
- CI/CD with GitHub Actions

## API Endpoints

- `GET /` - HTML interface for interacting with the queue
- `GET /api/tickets` - List all tickets in the queue
- `POST /api/tickets` - Create a new ticket (ticket description in request body)
- `DELETE /api/tickets` - Remove the next ticket from the queue
- `GET /api/tickets/next` - View the next ticket without removing it

## HTML Interface

The application includes a user-friendly HTML interface that allows you to interact with the ticket queue system through your web browser. The interface provides the following features:

- **Create New Tickets**: Enter a description and create a new ticket
- **View Next Ticket**: See the next ticket in the queue without removing it
- **Remove Next Ticket**: Process and remove the next ticket from the queue
- **View All Tickets**: See a list of all tickets currently in the queue

To access the HTML interface, simply navigate to `http://localhost:8080/` in your web browser after starting the application.

## Project Structure

```
.
├── src/
│   └── main/
│       ├── java/
│       │   └── org/
│       │       └── example/
│       │           ├── FIFOQueue.java  # FIFO queue implementation
│       │           ├── Main.java       # HTTP server and API endpoints
│       │           └── Ticket.java     # Ticket data model
│       └── resources/
│           └── static/
│               └── index.html     # HTML interface
├── .github/
│   └── workflows/
│       └── ci.yml                # GitHub Actions workflow
├── Dockerfile                    # Docker configuration
├── test.sh                       # Test script for API endpoints
├── test_interface.sh             # Script to test the HTML interface
├── build.sh                      # Build script
└── README.md                     # This file
```

## Prerequisites

- Java Development Kit (JDK) 17 or higher
- Docker (optional, for containerized deployment)
- curl (for running tests)

## Building and Running

### Using Java directly

1. Compile the Java files:
   ```bash
   javac -d classes src/main/java/org/example/*.java
   ```

2. Run the application:
   ```bash
   java -cp classes org.example.Main
   ```

### Using the build script

1. Make the script executable:
   ```bash
   chmod +x build.sh
   ```

2. Run the script:
   ```bash
   ./build.sh
   ```

### Using Docker

1. Build the Docker image:
   ```bash
   docker build -t ticket-queue-api .
   ```

2. Run the Docker container:
   ```bash
   docker run -p 8080:8080 ticket-queue-api
   ```

## Testing

### API Testing

The application includes a test script that verifies all API endpoints:

1. Make sure the application is running
2. Make the test script executable:
   ```bash
   chmod +x test.sh
   ```
3. Run the test script:
   ```bash
   ./test.sh
   ```

### HTML Interface Testing

To test the HTML interface:

1. Make the test script executable:
   ```bash
   chmod +x test_interface.sh
   ```
2. Run the test script:
   ```bash
   ./test_interface.sh
   ```
3. Open a web browser and navigate to:
   ```
   http://localhost:8080/
   ```
4. You should see the Ticket Queue System interface where you can:
   - Create new tickets
   - View the next ticket in the queue
   - Remove tickets from the queue
   - View all tickets in the queue

## Example Usage

### Create a new ticket

```bash
curl -X POST -d "Customer support request" http://localhost:8080/api/tickets
```

### List all tickets

```bash
curl http://localhost:8080/api/tickets
```

### View the next ticket

```bash
curl http://localhost:8080/api/tickets/next
```

### Remove the next ticket

```bash
curl -X DELETE http://localhost:8080/api/tickets
```

## License

This project is open source and available under the [MIT License](LICENSE).
