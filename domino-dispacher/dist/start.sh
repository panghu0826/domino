#!/bin/sh

kill -9 `ps -ef --cols=1000 | grep -v grep | grep com.jule.domino.dispacher.Main | grep java | awk '{print $2}'`;
sleep 5;

nohup java -Xms128m -Xmx128m -XX:+TieredCompilation -XX:+DisableExplicitGC -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication  -XX:+UseTLAB -ea -cp ./libs/*:./libs/domino-dispacher-1.0.0.jar com.jule.domino.dispacher.Main >/dev/null 2>&1 &

