@echo off
javac -d bin -sourcepath src .\src\ui\*.java
java -cp bin ui.Main
echo running Maze Runner...
pause