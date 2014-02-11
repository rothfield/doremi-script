#!/bin/bash

WATCHER_PID_FILE="watcher.pid"
if [ -e $WATCHER_PID_FILE ] 
then
		WATCHER_PID=$(cat $WATCHER_PID_FILE)
		kill -9 $WATCHER_PID
		rm $WATCHER_PID_FILE
else
		echo "watcher is not running.."
fi






