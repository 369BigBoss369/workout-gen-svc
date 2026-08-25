#!/bin/bash
echo "========================================"
echo "   Chat2API Setup for Workout Generator"
echo "========================================"
echo
echo "This script will install Chat2API for your workout generator."
echo "Chat2API provides free GPT-3.5 access for AI-powered workouts."
echo

if ! command -v git &> /dev/null; then
    echo "ERROR: Git is not installed. Please install Git first."
    echo "See: https://git-scm.com/downloads"
    exit 1
fi

PYTHON_CMD="python3"
if ! command -v python3 &> /dev/null; then
    if command -v python &> /dev/null; then
        PYTHON_CMD="python"
    else
        echo "ERROR: Python is not installed. Please install Python 3.8+ first."
        echo "See: https://python.org/downloads"
        exit 1
    fi
fi

echo "Installing Chat2API..."
echo

if [ ! -d "chat2api" ]; then
    echo "Cloning Chat2API repository..."
    git clone https://github.com/Niansuh/chat2api.git
    if [ $? -ne 0 ]; then
        echo "ERROR: Failed to clone Chat2API repository."
        exit 1
    fi
else
    echo "Chat2API directory already exists, updating..."
    (cd chat2api && git pull)
fi

echo
echo "Installing Python dependencies..."
cd chat2api || exit 1

if [ -f "utils/config.py" ]; then
    echo "Fixing filename issue: config.py -> configs.py"
    mv "utils/config.py" "utils/configs.py"
elif [ ! -f "utils/configs.py" ]; then
    echo "ERROR: Neither config.py nor configs.py found!"
    cd ..
    exit 1
fi

PIP_CMD="pip3"
if ! command -v pip3 &> /dev/null; then
    PIP_CMD="pip"
fi

"$PIP_CMD" install -r requirements.txt
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to install Python dependencies."
    echo "Make sure pip is installed and try again."
    cd ..
    exit 1
fi

cd ..
echo
echo "========================================"
echo "   Chat2API Setup Complete!"
echo "========================================"
echo
echo "Chat2API is now installed and ready to use."
echo
echo "To start your workout generator with Chat2API:"
echo "  ./mvnw spring-boot:run"
echo
echo "Chat2API will start automatically with your app."
echo
echo "To test manually: cd chat2api && python3 app.py"
echo
