# Maze-Runner
This project is for subject CPE231 Algorithm, optimizing the algorithm for solving a maze puzzle.

# How to run the project for dev.
1. The final maze solving UI.
- Compile: cd /home/user/Maze-Runner/mazeRunnerNW && javac -d bin src/com/nw/maze/*.java
- Run: cd /home/user/Maze-Runner/mazeRunnerNW && java -cp bin com.nw.maze.Main

2. The experimental one.
- On Windows, click runUI.bat
- On linux, use cd /home/user/Maze-Runner && ./runUI.sh

- Testing Dijkstra algorithm,
  - Compile: cd /home/user/Maze-Runner && javac -d bin -cp bin:src src/algorithm/Dijkstra.java && ./test_single.sh m15_15

  - Run: ./test_single.sh m15_15        # use any testcases that you want 

