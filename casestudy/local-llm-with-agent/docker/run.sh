set -e

docker-compose down

docker-compose up --build -d

echo "Waiting for the container to be ready..."
sleep 5
docker ps

