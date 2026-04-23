#!/bin/bash
set -e

cd "$(dirname "$0")"

echo "Building frontend..."
(
  cd agent/frontend
  npm install
  npm run build
)

echo ""
echo "Building uber JAR..."
cd ../..
./gradlew :casestudy:local-llm-with-agent:agent:uberJar
cd casestudy/local-llm-with-agent

echo ""
echo "✓ Build complete!"
echo "  Frontend: agent/src/main/resources/dist/"
echo "  JAR: agent/build/libs/agent-all.jar"
echo ""
echo "To start with Docker Compose:"
echo "  docker-compose up"

echo ""
echo "Building agent Docker image..."
docker build -t local-llm-agent ./agent

(
  cd docker
  docker-compose down
  docker-compose up -d

  echo "Waiting for the container to be ready..."
  sleep 5
  docker ps
)