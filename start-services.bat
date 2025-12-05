@echo off
echo Starting Kafka...
echo.

echo 1. Stopping old containers...
docker-compose down --remove-orphans 2>nul
timeout /t 2

echo.
echo 2. Starting Kafka...
docker-compose up -d
timeout /t 5

echo.
echo 3. Checking...
docker ps
echo.

echo 4. Open browser: http://localhost:8081
echo.
echo 5. If all 3 containers show as "Up", Kafka is ready!
echo.
pause