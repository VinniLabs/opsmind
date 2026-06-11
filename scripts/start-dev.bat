@echo off

cd /d C:\projetos\opsmind\backend

echo ====================================
echo Iniciando OpsMind
echo ====================================

docker compose down
docker compose up -d --build

echo.
echo Containers:
docker ps

echo.
echo Swagger:
echo http://localhost:8080/swagger-ui/index.html

echo.
echo Front:
echo http://localhost:5173

echo.
echo Logs:
docker logs -f opsmind-backend