@echo off
javac -d bin -sourcepath src .\src\th\ac\kmutt\cpe\algorithm\maze\*.java
java -cp bin th.ac.kmutt.cpe.algorithm.maze.Main
echo running Maze Runner...
pause