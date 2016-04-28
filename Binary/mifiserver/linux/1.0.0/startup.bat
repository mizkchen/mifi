@echo OFF
rem "JAVA_HOME environment variable is needed to run this program"
if "%JAVA_HOME%" == "" goto QUIT
if not exist "%JAVA_HOME%\bin\java.exe" goto QUIT
if not exist "%JAVA_HOME%\bin\javaw.exe" goto QUIT
if not exist "%JAVA_HOME%\bin\jdb.exe" goto QUIT
if not exist "%JAVA_HOME%\bin\javac.exe" goto QUIT
goto STARTUP

:QUIT
echo The JAVA_HOME environment variable is not defined correctly.
echo It is needed to run this program.
echo NB: JAVA_HOME should point to a JDK not a JRE.
goto END

:STARTUP
"%JAVA_HOME%\bin\java.exe" -jar mifiserver.jar server.json

:END
exit /b 1