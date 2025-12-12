#!/bin/bash

# Create bin directory if it doesn't exist
mkdir -p bin

# Compile Java files
echo "Compiling Java files..."
javac -d bin -sourcepath src .\src\th\ac\kmutt\cpe\algorithm\maze\*.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful. Running Maze Runner UI..."
    java -cp bin th.ac.kmutt.cpe.algorithm.maze.Main
else
    echo "Compilation failed!"
    exit 1
fi