@echo off
setlocal
cd /d "%~dp0.."
call mvn clean javafx:run
if errorlevel 1 pause
