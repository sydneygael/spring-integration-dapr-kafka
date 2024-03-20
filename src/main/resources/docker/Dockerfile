# Utilisation de l'image openjdk:21-jdk-slim
FROM openjdk:21-jdk-slim

# Mise à jour des paquets et installation de wget
RUN apt-get update && apt-get install -y wget

# Définition du répertoire de travail
WORKDIR /app

# Install dapr CLI
RUN wget -q https://raw.githubusercontent.com/dapr/cli/master/install/install.sh -O - | /bin/bash

# Copie des fichiers de configuration Dapr
COPY src/main/resources/docker/entrypoint.sh /app/entrypoint.sh
COPY src/main/resources/docker/dapr/components /app/dapr/components

# Copie des fichiers components
RUN chmod +x /app/entrypoint.sh

# Point d'entrée de l'application
CMD ["/app/entrypoint.sh"]
