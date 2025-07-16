# API de File d'Attente de Tickets

Une API REST simple pour gérer un système de file d'attente de tickets (FIFO), construite avec Java pur (sans frameworks).

## Fonctionnalités

- Implémentation d'une file d'attente FIFO avec des opérations standard (enqueue, dequeue, peek, isEmpty, size)
- Points d'API REST pour manipuler la file d'attente
- Interface HTML conviviale pour interagir avec la file d'attente
- Stockage de données en mémoire
- Application conteneurisée avec Docker
- Script de test complet
- CI/CD avec GitHub Actions

## Points d'API

- `GET /` - Interface HTML pour interagir avec la file d'attente
- `GET /api/tickets` - Lister tous les tickets dans la file d'attente
- `POST /api/tickets` - Créer un nouveau ticket (description du ticket dans le corps de la requête)
- `DELETE /api/tickets` - Supprimer le prochain ticket de la file d'attente
- `GET /api/tickets/next` - Voir le prochain ticket sans le supprimer
- `GET /api/counters` - Lister tous les guichets
- `POST /api/counters/:id` - Assigner un ticket à un guichet
- `POST /api/counters/:id/toggle` - Basculer la disponibilité d'un guichet
- `POST /api/counters/:id/end-session` - Terminer la session avec le client actuel

## Interface HTML

L'application inclut une interface HTML conviviale qui vous permet d'interagir avec le système de file d'attente de tickets via votre navigateur web. L'interface offre les fonctionnalités suivantes :

- **Créer de nouveaux tickets** : Entrez une description et créez un nouveau ticket
- **Voir le prochain ticket** : Consultez le prochain ticket dans la file d'attente sans le supprimer
- **Supprimer le prochain ticket** : Traitez et supprimez le prochain ticket de la file d'attente
- **Voir tous les tickets** : Consultez la liste de tous les tickets actuellement dans la file d'attente
- **Gérer les guichets** : Consultez l'état des guichets, assignez des tickets, et gérez leur disponibilité

Pour accéder à l'interface HTML, naviguez simplement vers `http://localhost:8080/` dans votre navigateur web après avoir démarré l'application.

## Structure du Projet

```
.
├── src/
│   └── main/
│       ├── java/
│       │   └── org/
│       │       └── example/
│       │           ├── FIFOQueue.java  # Implémentation de la file d'attente FIFO
│       │           ├── Main.java       # Serveur HTTP et points d'API
│       │           ├── Ticket.java     # Modèle de données du ticket
│       │           └── Counter.java    # Modèle de données du guichet
│       └── resources/
│           └── static/
│               └── index.html     # Interface HTML
├── .github/
│   └── workflows/
│       └── ci.yml                # Workflow GitHub Actions
├── Dockerfile                    # Configuration Docker
├── test.sh                       # Script de test pour les points d'API
├── test_interface.sh             # Script pour tester l'interface HTML
├── build.sh                      # Script de construction
└── README.md                     # Ce fichier
```

## Prérequis

- Kit de Développement Java (JDK) 17 ou supérieur
- Docker (optionnel, pour le déploiement conteneurisé)
- curl (pour exécuter les tests)

## Guide de Démarrage Rapide pour l'Évaluateur

### 1. Construction et Exécution

#### Utilisation du script de construction

1. Rendez le script exécutable :
   ```bash
   chmod +x build.sh
   ```

2. Exécutez le script :
   ```bash
   ./build.sh
   ```

   Ce script compile l'application et la démarre automatiquement. L'API sera disponible à l'adresse http://localhost:8080.

#### Utilisation de Docker

1. Construisez l'image Docker :
   ```bash
   docker build -t ticket-queue-api .
   ```

2. Exécutez le conteneur Docker :
   ```bash
   docker run -p 8080:8080 ticket-queue-api
   ```

   L'API sera disponible à l'adresse http://localhost:8080.

### 2. Test de l'Application

#### Test de l'API

1. Assurez-vous que l'application est en cours d'exécution
2. Rendez le script de test exécutable :
   ```bash
   chmod +x test.sh
   ```
3. Exécutez le script de test :
   ```bash
   ./test.sh
   ```

   Ce script vérifie tous les points d'API et affiche les résultats des tests.

#### Test de l'Interface HTML

1. Assurez-vous que l'application est en cours d'exécution
2. Ouvrez un navigateur web et naviguez vers :
   ```
   http://localhost:8080/
   ```
3. Vous devriez voir l'interface du Système de File d'Attente de Tickets où vous pouvez :
   - Créer de nouveaux tickets
   - Voir le prochain ticket dans la file d'attente
   - Supprimer des tickets de la file d'attente
   - Voir tous les tickets dans la file d'attente
   - Gérer les guichets

### 3. Exemples d'Utilisation de l'API avec curl

#### Créer un nouveau ticket

```bash
curl -X POST -d "Demande de support client" http://localhost:8080/api/tickets
```

#### Lister tous les tickets

```bash
curl http://localhost:8080/api/tickets
```

#### Voir le prochain ticket

```bash
curl http://localhost:8080/api/tickets/next
```

#### Supprimer le prochain ticket

```bash
curl -X DELETE http://localhost:8080/api/tickets
```

#### Lister tous les guichets

```bash
curl http://localhost:8080/api/counters
```

#### Assigner un ticket à un guichet

```bash
curl -X POST http://localhost:8080/api/counters/1
```

## Déploiement Continu

L'application est configurée avec GitHub Actions pour le déploiement continu. Lorsqu'une nouvelle version est publiée, l'image Docker est automatiquement construite et déployée sur Docker Hub, puis déployée sur le serveur de production.

## Licence

Ce projet est open source et disponible sous la [Licence MIT](LICENSE).
