# Deployment Guide

## Summary of Changes

### 1. **build.gradle** - Added JAR creation
   - Added `application` plugin with `AgentMain` as main class
   - Created `fatJar` task that bundles all dependencies into `agent-all.jar`
   - JAR includes Vert.x, ollama4j, and all dependencies
   - Size: ~16MB

### 2. **docker/agent/Dockerfile** - Container configuration
   - Copies `agent/build/libs/agent-all.jar` from build context
   - Sets environment variables for Ollama connection
   - Exposes port 8080
   - Runs with: `java -jar agent.jar`

### 3. **Frontend** - Vue.js UI
   - Built to `agent/src/main/resources/dist/`
   - Bundled into JAR at build time
   - Served by Vert.x at `http://localhost:8080`

### 4. **build.sh** - One-command build
   - Builds frontend
   - Builds fat JAR
   - Ready to deploy

## Build and Deploy Steps

### Option 1: Using the build script (Recommended)
```bash
cd /home/northd/Code/ai/casestudy/local-llm-with-agent
./build.sh
```

### Option 2: Manual steps
```bash
# Step 1: Build frontend
cd agent/frontend
npm install
npm run build
cd ../..

# Step 2: Build JAR
cd /home/northd/Code/ai
./gradlew :casestudy:local-llm-with-agent:agent:fatJar
```

### Step 3: Run with Docker Compose
```bash
cd /home/northd/Code/ai/casestudy/local-llm-with-agent
docker-compose build agent
docker-compose up
```

## Verification

### Check JAR was created:
```bash
ls -lh agent/build/libs/agent-all.jar
# Should show: agent-all.jar (16M)
```

### Check frontend was built:
```bash
ls agent/src/main/resources/dist/index.html
# Should exist
```

### Test locally (without Docker):
```bash
# Terminal 1: Start Ollama
docker run -d -p 11434:11434 ollama/ollama

# Terminal 2: Run agent
cd agent
../../../gradlew run

# Terminal 3: Access UI
# http://localhost:8080
```

## Docker Build Context

The compose.yaml has `context: .` which means:
- Build context is the root: `/home/northd/Code/ai/casestudy/local-llm-with-agent/`
- Dockerfile can reference: `agent/build/libs/agent-all.jar`
- This COPY will work correctly

## Network in Docker Compose

- All services use `network_mode: "host"`
- Agent connects to LLM at `http://llm:11434` (internal hostname resolution)
- UI accessible at `http://localhost:8080` (from host)

## Troubleshooting

### JAR not found in Docker build
- Make sure you ran `./build.sh` or `gradlew fatJar`
- Check: `ls agent/build/libs/agent-all.jar`

### Frontend not showing
- Verify `agent/src/main/resources/dist/index.html` exists
- The frontend is embedded in the JAR, served at `/`

### Connection to Ollama fails
- In Docker: LLM service must be running
- Check service names match in compose.yaml
- Verify OLLAMA_HOST env var points to correct service

### Port conflicts
- Change `SERVER_PORT` env var to use different port
- Or stop other services using port 8080

