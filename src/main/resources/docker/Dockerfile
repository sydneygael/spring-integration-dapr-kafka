# Utilisation de l'image openjdk:21-jdk-slim
FROM openjdk:21-jdk-slim

# Mise à jour des paquets et installation de wget
RUN apt-get update && apt-get install -y wget

# Définition du répertoire de travail
WORKDIR /app

# Point d'entrée de l'application
CMD ["java", "-jar", "/app/demo.jar"]
# pour debugger
#CMD ["bash", "-c", "while true; do sleep 60; done"]