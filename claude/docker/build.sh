set -e

OLLAMA_IMAGES=$(docker images --format table |grep ollama-with-codehelp || true)

if [ -n "$OLLAMA_IMAGES" ]; then
  echo "Image already exists."
  echo $OLLAMA_IMAGES

  exit 0
fi

OLLAMA_DISK_IMAGES=$(ls -al |grep ollama-with-codehelp || true)
if [ -n "$OLLAMA_DISK_IMAGES" ]; then
  echo "Image exists on disk. Loading ..."
  echo $OLLAMA_DISK_IMAGES
  docker load -i ollama-with-codehelp.tar
  docker images --format table | grep ollama
  exit 0
fi

docker build -t ollama-with-codehelp .
docker save -o ollama-with-codehelp.tar ollama-with-codehelp:latest
