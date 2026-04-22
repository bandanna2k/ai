# ollama4j Agent with UI

A Vert.x-based web application that provides an interactive UI for the ollama4j ReAct agent.

## Build

The build process compiles the Java code and builds the Vue.js frontend, creating a fat JAR that includes all dependencies.

### Quick Build

```bash
./build.sh
```

This will:
1. Build the Vue.js frontend (outputs to `agent/src/main/resources/dist/`)
2. Compile Java and create the fat JAR (`agent/build/libs/agent-all.jar`)

### Manual Build Steps

```bash
# Build frontend
cd agent/frontend
npm install
npm run build
cd ../..

# Build fat JAR
./gradlew :casestudy:local-llm-with-agent:agent:fatJar
```

## Docker

The agent container is configured in `compose.yaml` and uses the Dockerfile at `docker/agent/Dockerfile`.

### Build Image

```bash
docker-compose build agent
```

### Run with Docker Compose

```bash
docker-compose up
```

This will:
- Start the LLM service (Ollama) on port 11434
- Start the agent UI on port 8080
- Start the runner service on port 10201 and database

Access the UI at: http://localhost:8080

## Architecture

### Backend
- **Java + Vert.x**: HTTP server and REST API
- **Agent**: ReAct-style LLM agent with tool calling
- **Tools**: `get_time`, `calculate`

### Frontend
- **Vue 3**: Reactive component framework
- **Vite**: Fast dev server and build tool
- **Chat UI**: Interactive chat interface with reasoning visualization

## Environment Variables

### Agent Service

- `OLLAMA_HOST` (default: `http://localhost:11434`)
- `OLLAMA_MODEL` (default: `llama3.2`)
- `SERVER_PORT` (default: `8080`)

In Docker Compose, these are set to:
- `OLLAMA_HOST=http://llm:11434` (points to the llm service)
- `OLLAMA_MODEL=llama3.2`
- `SERVER_PORT=8080`

## Development

### Run Locally

Start the LLM service first:
```bash
docker run -d -p 11434:11434 ollama/ollama
```

Then run the agent:
```bash
cd agent
./gradlew run
```

Or in dev mode with live frontend updates:
```bash
# Terminal 1: Run the agent
cd agent
./gradlew run

# Terminal 2: Run the frontend dev server (hot reload)
cd agent/frontend
npm run dev
```

Then access the UI at: http://localhost:3000

## API

The agent provides a single REST endpoint:

### POST /v1/chat

Request:
```json
{
  "question": "What time is it?"
}
```

Response:
```json
{
  "answer": "The current time is 2025-04-22 15:30:45 GMT",
  "steps": [
    {
      "type": "THOUGHT",
      "content": "The user is asking for the current time..."
    },
    {
      "type": "ACTION",
      "content": "get_time()"
    },
    {
      "type": "OBSERVATION",
      "content": "2025-04-22 15:30:45 GMT"
    }
  ]
}
```

## File Structure

```
.
├── agent/                              # Main agent module
│   ├── build.gradle                   # Gradle build config with fatJar task
│   ├── frontend/                      # Vue.js frontend
│   │   ├── src/
│   │   │   ├── App.vue
│   │   │   ├── components/AgentChat.vue
│   │   │   ├── main.js
│   │   │   └── style.css
│   │   ├── package.json
│   │   └── vite.config.js
│   └── src/main/java/dnt/localagentapp/
│       ├── AgentMain.java             # Entry point, starts Vert.x server
│       ├── Agent.java                 # ReAct agent logic
│       ├── AgentResponse.java         # Response DTO
│       ├── AgentStep.java             # Step DTO
│       ├── AgentVerticle.java         # Vert.x HTTP handler
│       ├── Tool.java                  # Tool interface
│       └── Tools.java                 # Builtin tools
├── docker/
│   └── agent/Dockerfile               # Container definition
├── compose.yaml                       # Docker Compose configuration
└── build.sh                           # Build script
```

