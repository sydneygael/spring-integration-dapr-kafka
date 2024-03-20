#!/bin/sh
set -e

# Nom de l'application
app_id="demo-integration"

# Port de l'application
app_port=8080

# Port Dapr
dapr_port=3500

# Chemin des composants Dapr
components_path="/app/dapr/components"

# Chemin de l'application JAR
app_jar="/app/myapp.jar"

# config du dapr
config_path= "/app/dapr/config.yaml"

# Lancement du sidecar Dapr avec l'application Java
dapr run \
  --app-id "$app_id" \
  --app-port "$app_port" \
  --port "$dapr_port" \
  --config "$config_path" \
  --components-path "$components_path" \
  -- java -jar "$app_jar"

#!/bin/sh
set -e

# Lancement du sidecar Dapr avec l'application Java
dapr run \
  --app-id "mon-application" \
  --dapr-http-port 3500 \
  --app-port 8080 \
  --config /app/config.yaml \
  -- java -jar /app/mon-application.jar
