set -e

docker-compose down
docker-compose up -d

echo "Waiting for the container to be ready..."
sleep 5
docker ps

