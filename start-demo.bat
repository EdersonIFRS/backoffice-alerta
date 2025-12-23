@echo off
REM US#32 - Script para iniciar sistema em modo DEMO com seed de dados

echo ===============================================
echo  Backoffice Alerta - Modo DEMO
echo  US#32 - Ambiente com Dados Demonstrativos
echo ===============================================
echo.

echo [1/2] Compilando projeto...
call mvnw.cmd clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha na compilacao
    pause
    exit /b 1
)

echo.
echo [2/2] Iniciando aplicacao em modo DEMO...
echo.
echo  - Profile: demo
echo  - Database: H2 in-memory (backoffice_alerta_demo)
echo  - Seed: Habilitado
echo  - H2 Console: http://localhost:8080/h2-console
echo  - Frontend: http://localhost:3000
echo.

java -jar target\backoffice-alerta-1.0.0.jar --spring.profiles.active=demo

pause
