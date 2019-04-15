@echo off
title Juele Robot Service
:start
REM -------------------------------------
REM Default parameters for a basic server.
java -XX:+TieredCompilation -server -ea -Dio.netty.leakDetection.level=advanced -Xdebug -Xrunjdwp:transport=dt_socket,address=8999,server=y,suspend=n -Xms256m -Xmx256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+UseNUMA -XX:+UseTLAB  -cp ./libs/*;./libs/domino-robot-1.0.0.jar com.jule.domino.robot.Main
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
