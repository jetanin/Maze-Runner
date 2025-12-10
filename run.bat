@echo off
javac -d bin -sourcepath src .\src\*.java
java -cp bin Main
echo running Maze Runner...
pause