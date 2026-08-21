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
echo "Building uber JARs..."
cd ../..
#./gradlew :casestudy:local-llm-with-agent:agent:uberJar
./gradlew :casestudy:local-llm-with-agent:runner-app:generateSpec
./gradlew :casestudy:local-llm-with-agent:runner-app:uberJar
cd casestudy/local-llm-with-agent

echo ""
echo "✓ Build complete!"
echo "  Frontend: agent/src/main/resources/dist/"
echo "  JAR: agent/build/libs/agent-all.jar"
echo "  JAR: runner-app/build/libs/runner-app-all.jar"
echo "  OpenAPI: runner-app/build/generated/spec.yaml"
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

(
  # Probe MCP tools
  ENDPOINT="http://localhost:8081/runner/mcp"
  SESSION_ID=$(curl -sS -i -X POST "$ENDPOINT" \
    -H 'Content-Type: application/json' \
    -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"probe-tools","version":"1.0"}}}' \
    | sed -n 's/^Mcp-Session-Id: \(.*\)\r$/\1/p')

  curl -sS -X POST "$ENDPOINT" \
    -H 'Content-Type: application/json' \
    -H "Mcp-Session-Id: $SESSION_ID" \
    -d '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}' \
    | jq -r '.result.tools[].name'
)