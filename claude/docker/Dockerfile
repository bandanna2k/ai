FROM ollama/ollama:0.19.0-rc2

# Coding models
RUN ollama serve & sleep 5 && ollama pull deepseek-coder-v2:16b && pkill ollama
RUN ollama serve & sleep 5 && ollama pull qwen2.5:7b && pkill ollama