#!/bin/bash

echo "Installation de MongoDB via Docker"

# Chemin vers le répertoire contenant les fichiers JSON (depuis le script)
DATA_DIR="../data/"

# Se placer dans le répertoire contenant `docker-compose.yaml`
cd ../db/ || { echo "Le répertoire ../db/ est introuvable."; exit 1; }

echo "Répertoire courant : $(pwd)"

# Fonction de nettoyage en cas d'erreur
cleanup() {
  echo "Arrêt et suppression du conteneur MongoDB..."
  docker compose down
  exit 1
}

# Capture les signaux d'interruption pour exécuter la fonction de nettoyage
trap cleanup SIGINT SIGTERM

# Vérifie si le conteneur MongoDB existe
container_exists=$(docker ps -a --filter "name=mongodb-server" --format "{{.Names}}")

if [ -z "$container_exists" ]; then
  # Première exécution : démarre le conteneur et importe les données
  docker compose up -d

  # Vérifie si le conteneur a démarré correctement
  if [ $? -ne 0 ]; then
    echo "Erreur lors du démarrage du conteneur MongoDB."
    exit 1
  fi

  # Attend que MongoDB soit prêt
  echo "Attente du démarrage de MongoDB..."
  sleep 5

  echo "Contenu du répertoire des données :"
  ls "${DATA_DIR}"

  # Liste des collections et des fichiers correspondants
  declare -A collections=(
    ["films_embedded"]="films_embedded.json"
    ["utilisateurs_embedded"]="utilisateurs_embedded.json"
    ["films"]="films2.json"
    ["utilisateurs"]="utilisateurs2.json"
  )

  # Parcourt chaque collection pour l'importer
  for collection in "${!collections[@]}"; do
    file="${collections[$collection]}"

    # Vérifie si le fichier JSON existe
    if [ -f "${DATA_DIR}${file}" ]; then
      echo "Importation de la collection $collection depuis ${DATA_DIR}${file}..."

      # Détermine si l'option --jsonArray doit être utilisée
      json_import_options="--db info --collection $collection --file /import/${file}"
      if [[ "$file" == "films.json" ]]; then
        # Ne pas utiliser --jsonArray pour films.json
        echo "Importation sans l'option --jsonArray pour $file"
      else
        # Utiliser --jsonArray pour les autres fichiers
        json_import_options+=" --jsonArray"
      fi

      docker exec -i mongodb-server mongoimport $json_import_options

      # Vérifie si l'importation a réussi
      if [ $? -ne 0 ]; then
        echo "Erreur lors de l'importation de la collection $collection."
        cleanup
      fi
    else
      echo "Le fichier ${DATA_DIR}${file} est introuvable."
      cleanup
    fi
  done
else
  # Si le conteneur existe déjà, le redémarre simplement
  docker compose restart
fi

docker compose stats --no-stream
