#!/bin/bash

echo "Désinstallation de MongoDB via Docker"

cd ./../db/

docker compose rm --stop --force