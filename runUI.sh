#!/bin/bash

# Create bin directory if it doesn't exist
mkdir -p bin

# Compile Java files
echo "Compiling Java files..."
javac -d bin -sourcepath src src/ui/*.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful. Running Maze Runner UI..."
    java -cp bin ui.Main
else
    echo "Compilation failed!"
    exit 1
fi
