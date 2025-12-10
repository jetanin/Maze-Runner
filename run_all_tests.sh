#!/bin/bash
# Simple script to test each maze individually

echo "🔍 TESTING ALL 13 MAZE CASES 🔍"
echo "=================================="

# Compile first
echo "Compiling Dijkstra..."
javac -d bin -cp bin:src src/algorithm/Dijkstra.java src/ui/*.java

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo ""
    
    # Test each maze
    for maze in MAZE/*.txt; do
        echo "Testing: $maze"
        java -cp bin algorithm.Dijkstra "$maze"
        echo "---"
    done
    
    echo ""
    echo "🎉 All tests completed!"
else
    echo "✗ Compilation failed!"
fi
