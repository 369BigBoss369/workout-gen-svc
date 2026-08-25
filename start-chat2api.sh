#!/bin/bash
echo "Starting Chat2API server..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/chat2api" || { echo "ERROR: chat2api directory not found"; exit 1; }

if [ ! -f "app.py" ]; then
    echo "ERROR: app.py not found in chat2api directory"
    exit 1
fi

if [ -z "$CHAT2API_ACCESS_TOKEN" ]; then
    echo "WARNING: CHAT2API_ACCESS_TOKEN environment variable not set"
    echo "Chat2API may not work without authentication"
else
    echo "Setting AUTHORIZATION environment variable..."
    export AUTHORIZATION="$CHAT2API_ACCESS_TOKEN"
fi

PYTHON_CMD="python3"
if ! command -v python3 &> /dev/null; then
    PYTHON_CMD="python"
fi

echo "Running: $PYTHON_CMD app.py"
"$PYTHON_CMD" app.py
echo "Chat2API server stopped."
