#!/bin/sh

kill -9 `ps -ef --cols=1000 | grep -v grep | grep com.jule.domino.auth.Main | grep java | awk '{print $2}'`;
sleep 5;

nohup java -Xms128m -Xmx128m -XX:+DisableExplicitGC -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+UseNUMA -XX:+UseTLAB -ea -cp ./libs/*:./libs/domino-auth-1.0.0.jar com.jule.domino.auth.Main >/dev/null 2>&1 &

