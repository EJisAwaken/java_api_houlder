package org.example;

/**
 * Représente un guichet dans le système de file d'attente.
 */
public class Counter {
    private final String id;
    private final String name;
    private boolean available;
    private Ticket currentTicket;

    /**
     * Crée un nouveau guichet avec l'identifiant et le nom spécifiés.
     *
     * @param id l'identifiant du guichet
     * @param name le nom du guichet
     */
    public Counter(String id, String name) {
        this.id = id;
        this.name = name;
        this.available = false;
        this.currentTicket = null;
    }

    /**
     * Retourne l'identifiant du guichet.
     *
     * @return l'identifiant du guichet
     */
    public String getId() {
        return id;
    }

    /**
     * Retourne le nom du guichet.
     *
     * @return le nom du guichet
     */
    public String getName() {
        return name;
    }

    /**
     * Vérifie si le guichet est disponible.
     *
     * @return true si le guichet est disponible
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Définit la disponibilité du guichet.
     *
     * @param available true si le guichet est disponible
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Retourne le ticket actuellement traité par ce guichet.
     *
     * @return le ticket actuel ou null si aucun ticket n'est en cours de traitement
     */
    public Ticket getCurrentTicket() {
        return currentTicket;
    }

    /**
     * Définit le ticket actuellement traité par ce guichet.
     *
     * @param ticket le ticket à traiter
     */
    public void setCurrentTicket(Ticket ticket) {
        this.currentTicket = ticket;
    }

    /**
     * Retourne une représentation JSON du guichet.
     *
     * @return une chaîne JSON
     */
    public String toJson() {
        String ticketJson = currentTicket != null ? currentTicket.toJson() : "null";
        return String.format(
                "{\"id\": \"%s\", \"name\": \"%s\", \"available\": %b, \"currentTicket\": %s}",
                id,
                name.replace("\"", "\\\""),
                available,
                ticketJson
        );
    }
}
