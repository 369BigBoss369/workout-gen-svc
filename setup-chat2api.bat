@echo off
echo ========================================
echo    Chat2API Setup for Workout Generator
echo ========================================
echo.
echo This script will install Chat2API for your workout generator.
echo Chat2API provides free GPT-3.5 access for AI-powered workouts.
echo.

git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Git is not installed. Please install Git first.
    echo Download from: https://git-scm.com/downloads
    pause
    exit /b 1
)

python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Python is not installed. Please install Python 3.8+ first.
    echo Download from: https://python.org/downloads
    pause
    exit /b 1
)

echo Installing Chat2API...
echo.

if not exist chat2api (
    echo Cloning Chat2API repository...
    git clone https://github.com/Niansuh/chat2api.git
    if %errorlevel% neq 0 (
        echo ERROR: Failed to clone Chat2API repository.
        pause
        exit /b 1
    )
) else (
    echo Chat2API directory already exists, updating...
    cd chat2api
    git pull
    cd ..
)

echo.
echo Installing Python dependencies...
cd chat2api

if exist "utils\config.py" (
    echo Fixing filename issue: config.py -> configs.py
    move "utils\config.py" "utils\configs.py"
) else (
    echo WARNING: utils\config.py not found, checking for configs.py...
    if not exist "utils\configs.py" (
        echo ERROR: Neither config.py nor configs.py found!
        cd ..
        pause
        exit /b 1
    )
)

pip install -r requirements.txt
if %errorlevel% neq 0 (
    echo ERROR: Failed to install Python dependencies.
    echo Make sure pip is installed and try again.
    cd ..
    pause
    exit /b 1
)

cd ..
echo.
echo ========================================
echo    Chat2API Setup Complete!
echo ========================================
echo.
echo Chat2API is now installed and ready to use.
echo.
echo To start your workout generator with Chat2API:
echo   .\mvnw spring-boot:run
echo.
echo Chat2API will start automatically with your app.
echo.
echo To test manually: cd chat2api && python app.py
echo.
pause

