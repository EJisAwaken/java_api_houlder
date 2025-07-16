package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Représente un ticket dans le système de file d'attente.
 */
public class Ticket {
    private static int nextId = 1;
    private final int id;
    private final String description;
    private final LocalDateTime createdAt;
    private final String agencyName;
    private String counterNumber;
    private String status;

    /**
     * Crée un nouveau ticket avec la description donnée.
     *
     * @param description la description du ticket
     */
    public Ticket(String description) {
        this(description, "Agence Yas Ankorondrano", null);
    }

    /**
     * Crée un nouveau ticket avec la description, le nom de l'agence et le numéro de guichet donnés.
     *
     * @param description la description du ticket
     * @param agencyName le nom de l'agence
     * @param counterNumber le numéro de guichet
     */
    public Ticket(String description, String agencyName, String counterNumber) {
        this.id = nextId++;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.agencyName = agencyName;
        this.counterNumber = counterNumber;
        this.status = "en_attente"; // en_attente, en_traitement, terminé
    }

    /**
     * Returns the ticket ID.
     *
     * @return the ticket ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the ticket description.
     *
     * @return the ticket description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the ticket creation time.
     *
     * @return the ticket creation time
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the agency name.
     *
     * @return the agency name
     */
    public String getAgencyName() {
        return agencyName;
    }

    /**
     * Retourne le numéro de guichet.
     *
     * @return le numéro de guichet
     */
    public String getCounterNumber() {
        return counterNumber;
    }

    /**
     * Définit le numéro de guichet.
     *
     * @param counterNumber le numéro de guichet
     */
    public void setCounterNumber(String counterNumber) {
        this.counterNumber = counterNumber;
    }

    /**
     * Retourne le statut du ticket.
     *
     * @return le statut du ticket
     */
    public String getStatus() {
        return status;
    }

    /**
     * Définit le statut du ticket.
     *
     * @param status le statut du ticket
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Retourne une représentation JSON du ticket.
     *
     * @return une chaîne JSON
     */
    public String toJson() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return String.format(
                "{\"id\": %d, \"description\": \"%s\", \"createdAt\": \"%s\", \"agencyName\": \"%s\", \"counterNumber\": %s, \"status\": \"%s\"}",
                id,
                description.replace("\"", "\\\""),
                createdAt.format(formatter),
                agencyName.replace("\"", "\\\""),
                counterNumber != null ? "\"" + counterNumber.replace("\"", "\\\"") + "\"" : "null",
                status.replace("\"", "\\\"")
        );
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", agencyName='" + agencyName + '\'' +
                ", counterNumber='" + counterNumber + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
