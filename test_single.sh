#!/bin/bash
# Individual maze test helper

if [ $# -eq 0 ]; then
    echo "Usage: ./test_single.sh <maze_name>"
    echo ""
    echo "Available mazes:"
    ls MAZE/*.txt | sed 's|MAZE/||' | sed 's|\.txt||'
    echo ""
    echo "Examples:"
    echo "  ./test_single.sh m15_15"
    echo "  ./test_single.sh m80_50"
    echo "  ./test_single.sh m100_100"
    exit 1
fi

MAZE_NAME="$1"

# Add .txt extension if not provided
if [[ ! "$MAZE_NAME" == *.txt ]]; then
    MAZE_NAME="${MAZE_NAME}.txt"
fi

# Add MAZE/ prefix if not provided
if [[ ! "$MAZE_NAME" == MAZE/* ]]; then
    MAZE_NAME="MAZE/${MAZE_NAME}"
fi

# Check if file exists
if [ ! -f "$MAZE_NAME" ]; then
    echo "Error: Maze file '$MAZE_NAME' not found!"
    echo ""
    echo "Available mazes:"
    ls MAZE/*.txt | sed 's|MAZE/||' | sed 's|\.txt||'
    exit 1
fi

echo "🔍 Testing individual maze: $MAZE_NAME"
echo "========================================="

# Compile if needed
if [ ! -f "bin/algorithm/Dijkstra.class" ] || [ "src/algorithm/Dijkstra.java" -nt "bin/algorithm/Dijkstra.class" ]; then
    echo "Compiling..."
    javac -d bin -cp bin:src src/algorithm/Dijkstra.java src/ui/*.java
    if [ $? -ne 0 ]; then
        echo "Compilation failed!"
        exit 1
    fi
fi

# Run the test
java -cp bin algorithm.Dijkstra "$MAZE_NAME"
