@echo off
cd /d "%~dp0"
if not exist "out\production\Mario" mkdir "out\production\Mario"
dir /s /b src\*.java > sources.txt
javac -encoding windows-1250 -d "out\production\Mario" @sources.txt
del sources.txt
xcopy /s /y /i res out\production\Mario >nul
echo Uruchamiam gre Mario...
start javaw -cp "out\production\Mario" com.mario.Game
