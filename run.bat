@echo off
javac -d bin -sourcepath src .\src\th\ac\kmutt\cpe\algorithm\maze\*.java
echo running Maze Runner...
java -cp bin th.ac.kmutt.cpe.algorithm.maze.Main
exit
