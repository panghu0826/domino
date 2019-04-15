#!/bin/sh

echo '开始停log服'

./stop.sh
sleep 5;

echo '开始启动log服'

nohup java -Xms128m -Xmx128m -XX:+DisableExplicitGC -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+UseNUMA -XX:+UseTLAB -ea -cp ./libs/*:./libs/goldfast-log-1.0.0.jar  com.jule.goldfast.log.LogServer >/dev/null 2>&1 &

log_file=logs/console.log
tailf ${log_file}
