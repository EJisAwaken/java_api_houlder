package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Classe principale qui démarre le serveur HTTP et configure les points d'API.
 */
public class Main {
    // La file d'attente qui stockera nos tickets
    private static final FIFOQueue<Ticket> ticketQueue = new FIFOQueue<>();

    // Liste des guichets disponibles
    private static final List<Counter> counters = new ArrayList<>();

    // Map pour accéder rapidement aux guichets par ID
    private static final Map<String, Counter> counterMap = new HashMap<>();

    // Port du serveur
    private static final int PORT = 8080;

    /**
     * Initialise les guichets disponibles dans le système.
     */
    private static void initializeCounters() {
        // Créer plusieurs guichets
        Counter counter1 = new Counter("1", "Guichet 1");
        Counter counter2 = new Counter("2", "Guichet 2");
        Counter counter3 = new Counter("3", "Guichet 3");
        Counter counter4 = new Counter("4", "Guichet 4");

        // Ajouter les guichets à la liste
        counters.add(counter1);
        counters.add(counter2);
        counters.add(counter3);
        counters.add(counter4);

        // Ajouter les guichets à la map pour un accès rapide
        counterMap.put(counter1.getId(), counter1);
        counterMap.put(counter2.getId(), counter2);
        counterMap.put(counter3.getId(), counter3);
        counterMap.put(counter4.getId(), counter4);
    }

    public static void main(String[] args) throws IOException {
        // Initialiser les guichets
        initializeCounters();

        // Créer et démarrer le serveur HTTP
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Configurer les gestionnaires de contexte pour différents points d'API
        server.createContext("/api/tickets", new TicketHandler());
        server.createContext("/api/counters", new CounterHandler());
        server.createContext("/", new RootHandler());

        // Définir l'exécuteur à null pour utiliser l'exécuteur par défaut
        server.setExecutor(null);

        // Démarrer le serveur
        server.start();

        System.out.println("Serveur démarré sur le port " + PORT);
        System.out.println("Points d'API:");
        System.out.println("  GET    /api/tickets      - Lister tous les tickets");
        System.out.println("  POST   /api/tickets      - Créer un nouveau ticket");
        System.out.println("  DELETE /api/tickets      - Supprimer le prochain ticket");
        System.out.println("  GET    /api/tickets/next - Voir le prochain ticket sans le supprimer");
        System.out.println("  GET    /api/counters     - Lister tous les guichets");
        System.out.println("  POST   /api/counters/:id - Assigner un ticket à un guichet");
        System.out.println("  GET    /                 - Interface HTML");
    }

    /**
     * Gestionnaire pour les points d'API liés aux guichets.
     */
    static class CounterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            try {
                if (method.equals("GET")) {
                    // GET /api/counters - Liste tous les guichets
                    handleGetAllCounters(exchange);
                } else if (method.equals("POST")) {
                    // POST /api/counters/:id - Assigne un ticket à un guichet
                    if (path.endsWith("/toggle")) {
                        // POST /api/counters/:id/toggle - Bascule la disponibilité du guichet et assigne un ticket si disponible
                        // Extract the counterId from a path like "/api/counters/1/toggle"
                        String[] pathParts = path.split("/");
                        System.out.println("Path: " + path);
                        System.out.println("Path parts: " + java.util.Arrays.toString(pathParts));
                        if (pathParts.length >= 4) {
                            String counterId = pathParts[pathParts.length - 2];
                            System.out.println("CounterId: " + counterId);
                            handleToggleAvailability(exchange, counterId);
                        } else {
                            sendResponse(exchange, 400, "{\"erreur\": \"Chemin invalide\"}");
                        }
                    } else if (path.endsWith("/end-session")) {
                        // POST /api/counters/:id/end-session - Termine la session avec le client actuel
                        String[] pathParts = path.split("/");
                        if (pathParts.length >= 4) {
                            String counterId = pathParts[pathParts.length - 2];
                            handleEndSession(exchange, counterId);
                        } else {
                            sendResponse(exchange, 400, "{\"erreur\": \"Chemin invalide\"}");
                        }
                    } else {
                        String counterId = path.substring(path.lastIndexOf("/") + 1);
                        handleAssignTicket(exchange, counterId);
                    }
                } else {
                    sendResponse(exchange, 405, "{\"erreur\": \"Méthode non autorisée\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"erreur\": \"Erreur serveur: " + e.getMessage() + "\"}");
            }
        }

        /**
         * Gère GET /api/counters - Liste tous les guichets.
         */
        private void handleGetAllCounters(HttpExchange exchange) throws IOException {
            StringBuilder jsonResponse = new StringBuilder("[");

            boolean first = true;
            for (Counter counter : counters) {
                if (!first) {
                    jsonResponse.append(",");
                }

                jsonResponse.append(counter.toJson());
                first = false;
            }

            jsonResponse.append("]");

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 200, jsonResponse.toString());
        }

        /**
         * Gère POST /api/counters/:id - Assigne un ticket à un guichet.
         */
        private void handleAssignTicket(HttpExchange exchange, String counterId) throws IOException {
            // Vérifier si le guichet existe
            Counter counter = counterMap.get(counterId);
            if (counter == null) {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                sendResponse(exchange, 404, "{\"erreur\": \"Guichet non trouvé\"}");
                return;
            }

            // Vérifier si la file d'attente est vide
            if (ticketQueue.isEmpty()) {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                sendResponse(exchange, 404, "{\"erreur\": \"File d'attente vide\"}");
                return;
            }

            // Récupérer le prochain ticket
            Ticket nextTicket = ticketQueue.dequeue();

            // Assigner le ticket au guichet
            nextTicket.setCounterNumber(counter.getId());
            nextTicket.setStatus("en_traitement");
            counter.setCurrentTicket(nextTicket);

            // Créer une réponse JSON avec le ticket et le guichet
            String jsonResponse = String.format(
                "{\"ticket\": %s, \"counter\": %s, \"waitingCount\": %d}",
                nextTicket.toJson(),
                counter.toJson(),
                ticketQueue.size()
            );

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 200, jsonResponse);
        }

        /**
         * Gère POST /api/counters/:id/toggle - Bascule la disponibilité du guichet et assigne un ticket si disponible.
         */
        private void handleToggleAvailability(HttpExchange exchange, String counterId) throws IOException {
            // Vérifier si le guichet existe
            Counter counter = counterMap.get(counterId);
            if (counter == null) {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                sendResponse(exchange, 404, "{\"erreur\": \"Guichet non trouvé\"}");
                return;
            }

            // Basculer la disponibilité du guichet
            boolean newAvailability = !counter.isAvailable();
            counter.setAvailable(newAvailability);

            // Si le guichet devient disponible et qu'il y a des tickets en attente, assigner un ticket
            if (newAvailability && !ticketQueue.isEmpty()) {
                // Récupérer le prochain ticket
                Ticket nextTicket = ticketQueue.dequeue();

                // Assigner le ticket au guichet
                nextTicket.setCounterNumber(counter.getId());
                nextTicket.setStatus("en_traitement");
                counter.setCurrentTicket(nextTicket);

                // Créer une réponse JSON avec le ticket et le guichet
                String jsonResponse = String.format(
                    "{\"ticket\": %s, \"counter\": %s, \"waitingCount\": %d, \"available\": %b}",
                    nextTicket.toJson(),
                    counter.toJson(),
                    ticketQueue.size(),
                    newAvailability
                );

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                sendResponse(exchange, 200, jsonResponse);
            } else {
                // Si le guichet devient occupé ou s'il n'y a pas de tickets en attente
                String jsonResponse = String.format(
                    "{\"counter\": %s, \"available\": %b}",
                    counter.toJson(),
                    newAvailability
                );

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                sendResponse(exchange, 200, jsonResponse);
            }
        }

        /**
         * Gère POST /api/counters/:id/end-session - Termine la session avec le client actuel.
         */
        private void handleEndSession(HttpExchange exchange, String counterId) throws IOException {
            // Vérifier si le guichet existe
            Counter counter = counterMap.get(counterId);
            if (counter == null) {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                sendResponse(exchange, 404, "{\"erreur\": \"Guichet non trouvé\"}");
                return;
            }

            // Vérifier si le guichet a un ticket en cours
            Ticket currentTicket = counter.getCurrentTicket();
            if (currentTicket == null) {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                sendResponse(exchange, 400, "{\"erreur\": \"Pas de ticket en cours pour ce guichet\"}");
                return;
            }

            // Marquer le ticket comme terminé
            currentTicket.setStatus("terminé");

            // Libérer le guichet
            counter.setCurrentTicket(null);
            counter.setAvailable(true);

            // Créer une réponse JSON avec le ticket et le guichet
            String jsonResponse = String.format(
                "{\"ticket\": %s, \"counter\": %s, \"message\": \"Session terminée avec succès\"}",
                currentTicket.toJson(),
                counter.toJson()
            );

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 200, jsonResponse);
        }

        /**
         * Méthode auxiliaire pour envoyer des réponses HTTP.
         */
        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    /**
     * Gestionnaire pour le point d'API racine qui sert l'interface HTML.
     */
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/")) {
                // Servir l'interface HTML
                try (InputStream is = Main.class.getClassLoader().getResourceAsStream("static/index.html")) {
                    if (is == null) {
                        // Si le fichier n'est pas trouvé, retourner une erreur 404
                        String notFound = "Interface HTML non trouvée";
                        exchange.getResponseHeaders().set("Content-Type", "text/plain");
                        exchange.sendResponseHeaders(404, notFound.getBytes().length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(notFound.getBytes());
                        }
                        return;
                    }

                    // Lire le fichier HTML
                    String html = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));

                    // Définir le type de contenu et envoyer la réponse
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, html.getBytes().length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(html.getBytes());
                    }
                }
            } else {
                // Pour tout autre chemin sous la racine, afficher les informations de l'API
                String response = "API de File d'Attente de Tickets\n\n" +
                        "Points d'API disponibles:\n" +
                        "GET    /api/tickets      - Lister tous les tickets\n" +
                        "POST   /api/tickets      - Créer un nouveau ticket\n" +
                        "DELETE /api/tickets      - Supprimer le prochain ticket\n" +
                        "GET    /api/tickets/next - Voir le prochain ticket sans le supprimer\n" +
                        "GET    /api/counters     - Lister tous les guichets\n" +
                        "POST   /api/counters/:id - Assigner un ticket à un guichet\n" +
                        "GET    /                 - Interface HTML\n";

                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, response.getBytes().length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }

    /**
     * Gestionnaire pour le point d'API /api/tickets qui gère les opérations de tickets.
     */
    static class TicketHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            try {
                switch (method) {
                    case "GET":
                        if (path.endsWith("/next")) {
                            handlePeekTicket(exchange);
                        } else {
                            handleGetAllTickets(exchange);
                        }
                        break;
                    case "POST":
                        handleCreateTicket(exchange);
                        break;
                    case "DELETE":
                        handleRemoveTicket(exchange);
                        break;
                    default:
                        sendResponse(exchange, 405, "{\"erreur\": \"Méthode non autorisée\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"erreur\": \"Erreur serveur: " + e.getMessage() + "\"}");
            }
        }

        /**
         * Gère GET /api/tickets - Liste tous les tickets dans la file d'attente.
         */
        private void handleGetAllTickets(HttpExchange exchange) throws IOException {
            List<Ticket> tickets = ticketQueue.getAll();
            StringBuilder jsonResponse = new StringBuilder("[");

            boolean first = true;
            for (int i = 0; i < tickets.size(); i++) {
                Ticket ticket = tickets.get(i);
                if (!first) {
                    jsonResponse.append(",");
                }

                // Calculer le nombre de personnes en attente pour ce ticket (nombre de tickets après celui-ci)
                int waitingCount = tickets.size() - i - 1;

                // Créer un objet JSON avec le ticket et le nombre de personnes en attente
                String ticketJson = String.format(
                    "{\"ticket\": %s, \"waitingCount\": %d}",
                    ticket.toJson(),
                    waitingCount
                );

                jsonResponse.append(ticketJson);
                first = false;
            }

            jsonResponse.append("]");

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 200, jsonResponse.toString());
        }

        /**
         * Gère POST /api/tickets - Crée un nouveau ticket.
         */
        private void handleCreateTicket(HttpExchange exchange) throws IOException {
            // Lire le corps de la requête pour obtenir la description du ticket
            String requestBody = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            if (requestBody == null || requestBody.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"erreur\": \"La description du ticket est requise\"}");
                return;
            }

            // Créer et mettre en file d'attente le nouveau ticket
            Ticket newTicket = new Ticket(requestBody);
            ticketQueue.enqueue(newTicket);

            // Obtenir le nombre de personnes en attente dans la file (excluant le ticket nouvellement créé)
            int waitingCount = ticketQueue.size() - 1;

            // Créer une réponse JSON avec le nouveau ticket et le nombre de personnes en attente
            String jsonResponse = String.format(
                "{\"ticket\": %s, \"waitingCount\": %d}",
                newTicket.toJson(),
                waitingCount
            );

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 201, jsonResponse);
        }

        /**
         * Gère DELETE /api/tickets - Supprime le prochain ticket de la file d'attente.
         */
        private void handleRemoveTicket(HttpExchange exchange) throws IOException {
            if (ticketQueue.isEmpty()) {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                sendResponse(exchange, 404, "{\"erreur\": \"La file d'attente est vide\"}");
                return;
            }

            Ticket removedTicket = ticketQueue.dequeue();

            // Marquer le ticket comme terminé
            removedTicket.setStatus("terminé");

            // Obtenir le nombre de personnes en attente dans la file après la suppression
            int waitingCount = ticketQueue.size();

            // Créer une réponse JSON avec le ticket supprimé et le nombre de personnes en attente
            String jsonResponse = String.format(
                "{\"ticket\": %s, \"waitingCount\": %d}",
                removedTicket.toJson(),
                waitingCount
            );

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 200, jsonResponse);
        }

        /**
         * Gère GET /api/tickets/next - Affiche le prochain ticket sans le supprimer.
         */
        private void handlePeekTicket(HttpExchange exchange) throws IOException {
            if (ticketQueue.isEmpty()) {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                sendResponse(exchange, 404, "{\"erreur\": \"La file d'attente est vide\"}");
                return;
            }

            Ticket nextTicket = ticketQueue.peek();

            // Obtenir le nombre de personnes en attente dans la file (excluant le ticket actuel)
            int waitingCount = ticketQueue.size() - 1;

            // Créer une réponse JSON avec le ticket et le nombre de personnes en attente
            String jsonResponse = String.format(
                "{\"ticket\": %s, \"waitingCount\": %d}",
                nextTicket.toJson(),
                waitingCount
            );

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 200, jsonResponse);
        }

        /**
         * Helper method to send HTTP responses.
         */
        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
