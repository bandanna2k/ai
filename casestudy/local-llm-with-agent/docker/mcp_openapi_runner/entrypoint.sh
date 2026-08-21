#!/bin/sh
set -eu

SPEC_PATH="${SPEC_PATH:-/opt/mcp/spec.yaml}"
API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
DEBUG="${DEBUG:-0}"
KEEP_ALIVE_ON_EXIT="${KEEP_ALIVE_ON_EXIT:-1}"

if [ ! -f "$SPEC_PATH" ]; then
  echo "ERROR: OpenAPI spec not found at $SPEC_PATH" >&2
  exit 1
fi

# awslabs/openapi-mcp-server reads uppercase env names in load_config().
export API_SPEC_PATH="$SPEC_PATH"
export API_BASE_URL="$API_BASE_URL"
export SERVER_DEBUG="$DEBUG"

cd /opt/mcp
set +e
python3 -c "from awslabs.openapi_mcp_server.server import main; main()"
exit_code=$?
set -e

if [ "$KEEP_ALIVE_ON_EXIT" = "1" ]; then
  echo "openapi-mcp-server exited with code $exit_code (stdio transport is expected to exit in detached containers)." >&2
  echo "Keeping container alive for inspection; set KEEP_ALIVE_ON_EXIT=0 to disable." >&2
  tail -f /dev/null
fi

exit "$exit_code"

