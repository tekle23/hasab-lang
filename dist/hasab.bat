@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "JAR_PATH=%SCRIPT_DIR%hasab.jar"

if not exist "%JAR_PATH%" (
    echo Error: hasab.jar not found at "%JAR_PATH%" >&2
    echo Please ensure the HASAB CLI is installed correctly. >&2
    exit /b 1
)

java -jar "%JAR_PATH%" %*
exit /b %errorlevel%
