#!/bin/bash

# Vérifier si MongoDB est installé
if ! command -v mongo &> /dev/null; then
    echo "MongoDB n'est pas installé. Veuillez l'installer avant d'exécuter ce script."
    exit 1
fi

echo "MongoDB est installé."

# Nom de la base de données
DB_NAME="info"

# Vérifier si la base de données existe
DB_EXISTS=$(mongo --quiet --eval "db.getMongo().getDBNames().indexOf('$DB_NAME') >= 0")

if [ "$DB_EXISTS" = "true" ]; then
    echo "Suppression de la base de données '$DB_NAME'..."
    mongo "$DB_NAME" --eval "db.dropDatabase()"

    # Vérifier si la suppression a réussi
    DB_EXISTS_AFTER=$(mongo --quiet --eval "db.getMongo().getDBNames().indexOf('$DB_NAME') >= 0")
    if [ "$DB_EXISTS_AFTER" = "false" ]; then
        echo "La base de données '$DB_NAME' a été supprimée avec succès."
    else
        echo "Erreur lors de la suppression de la base de données '$DB_NAME'."
        exit 1
    fi
else
    echo "La base de données '$DB_NAME' n'existe pas. Rien à supprimer."
fi
