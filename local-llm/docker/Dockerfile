FROM ollama/ollama:0.19.0-rc2

# Coding models
RUN ollama serve & sleep 5 && ollama pull deepseek-coder-v2:16b && pkill ollama
RUN ollama serve & sleep 5 && ollama pull qwen2.5:7b && pkill ollama

# LLM
RUN ollama serve & sleep 5 && ollama pull llama3.2 && pkill ollama

# LLM (fast)
RUN ollama serve & sleep 5 && ollama pull wizardlm2:7b && pkill ollama

# LLM Gaurd Rails
RUN ollama serve & sleep 5 && ollama pull llama-guard3:8b && pkill ollama

# LLM Guard Rails (fast)
RUN ollama serve & sleep 5 && ollama pull llama-guard3:1b && pkill ollama

# Imagery model
# RUN ollama serve & sleep 5 && ollama pull moondream:1.8b && pkill ollama

