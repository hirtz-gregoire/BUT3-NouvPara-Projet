#!/bin/bash

# Vérifier si MongoDB est installé
if ! command -v mongo &> /dev/null; then
    echo "MongoDB n'est pas installé. Veuillez l'installer avant d'exécuter ce script."
    exit 1
fi

echo "MongoDB est installé."

# Nom de la base de données
DB_NAME="info"

# Vérifier si la base de données existe déjà
DB_EXISTS=$(mongo --quiet --eval "db.getMongo().getDBNames().indexOf('$DB_NAME') >= 0")

if [ "$DB_EXISTS" = "true" ]; then
    echo "La base de données '$DB_NAME' existe déjà. Annulation du script."
    exit 1
else
    echo "Création de la base de données '$DB_NAME' et importation des collections..."
fi

# Chemin vers le répertoire contenant les fichiers JSON (depuis le répertoire 'doc')
DATA_DIR="./../data/"

# Vérifier si le répertoire DATA_DIR existe
if [ ! -d "$DATA_DIR" ]; then
    echo "Le répertoire $DATA_DIR n'existe pas."
    exit 1
fi

# Définir les collections et les fichiers correspondants
declare -A collections=(
    ["films_embedded"]="films_embedded.json"
    ["utilisateurs_embedded"]="utilisateurs_embedded.json"
    ["films"]="films2.json"
    ["utilisateurs"]="utilisateurs2.json"
)

# Parcourir les collections et importer les données
for collection in "${!collections[@]}"; do
    file="${collections[$collection]}"

    # Vérifier si le fichier existe
    if [ ! -f "${DATA_DIR}${file}" ]; then
        echo "Le fichier ${DATA_DIR}${file} est introuvable."
        exit 1
    fi

    # Importer les données
    echo "Importation de la collection '$collection' depuis le fichier '${DATA_DIR}${file}'..."

    # Déterminer si l'option --jsonArray est nécessaire
    # Supposons que les fichiers se terminant par '_embedded.json' sont des tableaux JSON
    if [[ "$file" == *"_embedded.json" ]]; then
        mongoimport --db "$DB_NAME" --collection "$collection" --file "${DATA_DIR}${file}" --jsonArray
    else
        mongoimport --db "$DB_NAME" --collection "$collection" --file "${DATA_DIR}${file}"
    fi

    # Vérifier si l'importation a réussi
    if [ $? -ne 0 ]; then
        echo "Erreur lors de l'importation de la collection '$collection'."
        exit 1
    fi
done

echo "Toutes les collections ont été importées avec succès dans la base de données '$DB_NAME'."
