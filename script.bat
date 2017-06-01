@echo off
if "%1" == "" (
    echo Usage: %0 path_to_bxml_file
    exit /b 1
)
java org.apache.pivot.wtk.ScriptApplication "--src=%1" 2>nul
if errorlevel 1 (call setenv.bat) & (java org.apache.pivot.wtk.ScriptApplication "--src=%1") & (call unsetenv.bat)
