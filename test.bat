@echo off
REM ========================================================
REM Laundry Service Order Tracker - Test Suite Runner
REM ========================================================

echo Compiling and running automated test suite...
if not exist "bin" mkdir bin
javac -d bin -encoding UTF-8 src/com/laundry/*.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

java -cp bin com.laundry.ProjectTestSuite
pause
