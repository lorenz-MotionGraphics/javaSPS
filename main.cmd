@echo off
cls
echo ===========================
echo   Solar Project Executor
echo ===========================
echo.

echo [1/3] Compiling solar.java...
javac -cp ".;gson-2.10.1.jar;jansi-2.4.0.jar" solar.java
if %errorlevel% neq 0 (
    echo.
    echo ❌ Compilation failed. Please fix the errors above.
    pause
    exit /b
)
echo Compilation successful!
echo.

set /p runGit=Do you want to run Git commands (Y/N)? 
if /i "%runGit%"=="Y" (
    echo.
    echo [2/3] Running Git commands...
    git add .
    if %errorlevel% neq 0 (
        echo ❌ Git add failed.
        pause
        exit /b
    )

    git commit -m "latest"
    if %errorlevel% neq 0 (
        echo ❌ Git commit failed.
        pause
        exit /b
    )

    git push
    if %errorlevel% neq 0 (
        echo ❌ Git push failed.
        pause
        exit /b
    )

    git status
    echo ✅ Git commands completed!
) else (
    echo ⚠️ Skipping Git commands.
)
echo.

echo [3/3] Running Java program...
java -cp ".;gson-2.10.1.jar;jansi-2.4.0.jar" solar
if %errorlevel% neq 0 (
    echo ❌ Java execution failed.
    pause
    exit /b
)
echo ✅ Program finished successfully!
pause
exit /b