#!/usr/bin/env bash
set -euo pipefail

ENDPOINT="http://localhost:8081/runner/mcp"
SESSION_ID=$(curl -sS -i -X POST "$ENDPOINT" \
  -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"probe-tools","version":"1.0"}}}' \
  | awk -F': ' '/^Mcp-Session-Id:/ {print $2}' | tr -d '\r')

curl -sS -X POST "$ENDPOINT" \
  -H 'Content-Type: application/json' \
  -H "Mcp-Session-Id: $SESSION_ID" \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}' | jq -r '.result.tools[].name'

