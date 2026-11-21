@echo off
REM ========================================================
REM Laundry Service Order Tracker - Compile and Run Script
REM ========================================================

echo [1/2] Compiling Java source files...
if not exist "bin" mkdir bin
javac -d bin -encoding UTF-8 src/com/laundry/*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Please check the Java files.
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Launching Laundry Service Order Tracker...
echo.
java -cp bin com.laundry.Main

pause
