@echo off
title Juele Gate Service
:start
REM -------------------------------------
REM Default parameters for a basic server.
java -XX:+TieredCompilation -server -ea -Dio.netty.leakDetection.level=advanced -Xms128m -Xmx128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+UseNUMA -XX:+UseTLAB  -cp ./libs/*;./libs/domino-gate-1.0.0.jar com.jule.domino.gate.Main all
REM -------------------------------------

SET CLASSPATH=%OLDCLASSPATH%

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
if ERRORLEVEL 0 goto end

REM Restart...
:restart
echo.
echo Administrator Restart ...
echo.
goto start

REM Error...
:error
echo.
echo Server terminated abnormaly ...
echo.
goto end

REM End...
:end
echo.
echo Server terminated ...
echo.
pause
