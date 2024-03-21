# Guide d'utilisation de l'application avec Docker et Dapr

## Prérequis

Assurez-vous d'avoir Docker et Dapr installés sur votre système avant de continuer.

- Pour installer Dapr, suivez les instructions dans la [documentation Dapr](https://docs.dapr.io/getting-started/install-dapr-cli/).

## Étape 1 : Lancer les services Kafka et PostgreSQL avec Docker Compose

Pour lancer uniquement les services Kafka et PostgreSQL à partir du fichier docker-compose.yaml, vous pouvez utiliser la commande suivante :

```bash
docker-compose up zookeeper kafka postgres pgadmin
```

#### v2
```bash
docker compose up zookeeper kafka postgres pgadmin
```

## Etape 2 : Lancer l'application

### en local

```bash
dapr run --app-id dapr-kafka --app-port 9000 --config src\main\resources\docker\dapr\config.yaml --resources-path src\main\resources\docker\dapr\components
```

### via gradle et docker compose

Pas besoin d'étape 1 dans ce cas

```bash
./gradlew clean runApp
```

