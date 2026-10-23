@echo off
cd /d "%~dp0"
if not exist build mkdir build
javac -encoding UTF-8 -d build src\*.java
if errorlevel 1 exit /b 1
if "%~1"=="--test" (
 java -ea -cp build ProjectTests
) else if "%~1"=="--mysql" (
 java -cp "build;.;lib/*" MainDB
) else (
 java -cp build MainDB --demo
)
