@echo off
if "%1" == "" (
    echo Usage: %0 path_to_bxml_file
    exit /b 1
)
java org.apache.pivot.wtk.ScriptApplication "--src=%1"
