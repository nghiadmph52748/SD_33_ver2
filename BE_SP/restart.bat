@echo off
echo Dang dung Spring Boot application...
taskkill /f /im java.exe 2>nul
timeout /t 3 /nobreak >nul

echo Dang khoi dong lai Spring Boot application...
cd /d "%~dp0"
mvn spring-boot:run

pause
