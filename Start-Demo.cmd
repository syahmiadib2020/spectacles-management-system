@echo off
setlocal
cd /d "%~dp0"
where java >nul 2>nul
if errorlevel 1 (
  echo Please install Java 17 or later and run this file again.
  pause
  exit /b 1
)
if not exist "target\meteor-eyewear-1.0.0.jar" (
  call mvnw.cmd --batch-mode --no-transfer-progress package
  if errorlevel 1 (
    echo Build failed. Please review the messages above.
    pause
    exit /b 1
  )
)
echo.
echo Meteor Eyewear local demo: http://localhost:8080
echo Keep this window open. Press Ctrl+C to stop.
java -jar target\meteor-eyewear-1.0.0.jar
if errorlevel 1 pause
