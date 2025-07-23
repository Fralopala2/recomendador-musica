#!/usr/bin/env bash

# Ejecuta el Maven Wrapper para construir el proyecto
# Render ya tiene Java preinstalado para proyectos Java.
# Asegúrate de que mvnw tiene permisos de ejecución en tu sistema local
# (git add y git commit deberían mantenerlos si ya los tenías).
chmod +x ./mvnw
./mvnw clean install