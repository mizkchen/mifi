@ECHO OFF
SET status=1 
(TASKLIST|FIND /I "java.exe"||SET status=0) 2>nul 1>nul
IF %status% EQU 1 (TASKKILL /f /t /im java.exe)
