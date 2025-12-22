FROM nvidia/cuda:12.8.0-cudnn-runtime-ubuntu22.04

# Prevent interactive prompts
ENV DEBIAN_FRONTEND=noninteractive
ENV PYTHONUNBUFFERED=1

# Install system dependencies
RUN apt-get update && apt-get install -y \
    python3.10 \
    python3-pip \
    git \
    wget \
    libgl1 \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Clone ComfyUI
RUN git clone https://github.com/comfyanonymous/ComfyUI.git /app/ComfyUI

WORKDIR /app/ComfyUI

# Install Python dependencies
RUN pip3 install --no-cache-dir torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu121
RUN pip3 install --no-cache-dir -r requirements.txt

# Create model directories
RUN mkdir -p models/checkpoints models/vae models/loras models/controlnet models/clip

# Download SDXL Base model
RUN wget -O models/checkpoints/sd_xl_base_1.0.safetensors \
    https://huggingface.co/stabilityai/stable-diffusion-xl-base-1.0/resolve/main/sd_xl_base_1.0.safetensors

# Download SDXL Refiner (optional but recommended)
RUN wget -O models/checkpoints/sd_xl_refiner_1.0.safetensors \
    https://huggingface.co/stabilityai/stable-diffusion-xl-refiner-1.0/resolve/main/sd_xl_refiner_1.0.safetensors

# Download VAE
RUN wget -O models/vae/sdxl_vae.safetensors \
    https://huggingface.co/stabilityai/sdxl-vae/resolve/main/sdxl_vae.safetensors

# Download CLIP models (required for SDXL)
RUN wget -O models/clip/clip_l.safetensors \
    https://huggingface.co/comfyanonymous/flux_text_encoders/resolve/main/clip_l.safetensors

# Expose port
EXPOSE 8188

# Run ComfyUI (CPU mode)
CMD ["python3", "main.py", "--listen", "0.0.0.0", "--port", "8188", "--cpu"]