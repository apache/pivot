@echo off
set CLASSPATH_OLD=%CLASSPATH%
for %%F in (%CD%\lib\pivot*.jar) do call addclass %%F
for %%F in (%CD%\wtk\lib\*.jar) do call addclass %%F
echo CLASSPATH=%CLASSPATH%
