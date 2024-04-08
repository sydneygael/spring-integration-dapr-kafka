# Guide d'utilisation de l'application avec Docker et Dapr

## Prérequis

Assurez-vous d'avoir Docker et Dapr installés sur votre système avant de continuer.

- Pour installer Dapr, suivez les instructions dans la [documentation Dapr](https://docs.dapr.io/getting-started/install-dapr-cli/).

## Étape 1 : Lancer les services Kafka et PostgreSQL avec Docker Compose

Pour lancer uniquement les services Kafka et PostgreSQL à partir du fichier docker-compose.yaml, vous pouvez utiliser la commande suivante :

```bash
docker-compose up zookeeper kafka akhq postgres
```

#### v2
```bash
docker compose up zookeeper kafka akhq postgres
```

### Notes
Vous pouvez également lancer pgadmin pour voir les données en base si vous le désirer

## Etape 2 : Lancer l'application

### toujours lancer dapr

```bash
dapr run --app-id dapr-kafka --app-port 9000 --dapr-http-port 3500 --config src\main\resources\dapr\config.yaml --resources-path src\main\resources\dapr\components
```

### en local

```bash
./gradlew copyToResources
./gradlew bootRun
```
### via gradle et docker compose

Notez bien qu'on utilise les dernières versions de docker compose intégrées directement à docker

```bash
./gradlew copyToResources
docker compose up -d
```

## Créer les topics 

Pas besoin de créer les topics en temps réel car DAPR s'en charge mais au besoin

```bash
docker compose exec kafka /opt/kafka_2.13-2.8.1/bin/kafka-topics.sh --create --bootstrap-server kafka:29092 --replication-factor 1 --partitions 1 --topic error-topic

docker compose exec kafka /opt/kafka_2.13-2.8.1/bin/kafka-topics.sh --create --bootstrap-server kafka:29092 --replication-factor 1 --partitions 1 --topic pizza-topic

docker compose exec kafka /opt/kafka_2.13-2.8.1/bin/kafka-topics.sh --create --bootstrap-server kafka:29092 --replication-factor 1 --partitions 1 --topic pasta-topic
```

