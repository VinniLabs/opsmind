@echo off

cd /d C:\projetos\opsmind\backend

echo ====================================
echo Limpando containers orfaos
echo ====================================

docker compose down --remove-orphans

echo ====================================
echo Iniciando OpsMind
echo ====================================

docker compose up -d

echo.
echo Containers:
docker ps

echo.
echo Ollama:
echo http://localhost:11434

echo.
echo Swagger:
echo http://localhost:8080/swagger-ui/index.html

echo.
echo Front:
echo http://localhost:5173

echo.
echo Logs:
docker logs -f opsmind-backend
