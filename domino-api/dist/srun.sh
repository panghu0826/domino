#!/bin/bash
kill -9 `ps -ef --cols=1000 | grep -v grep | grep com.boot.AppApi | grep java | awk '{print $2}'`;
sleep 5;
nohup java -Xms128m -Xmx128m -XX:+DisableExplicitGC -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+UseNUMA -XX:+UseTLAB -ea -cp ./libs/*:./libs/jule-api-1.0.0.jar com.boot.AppApi >/dev/null 2>&1 &

tail -f logs/login.log