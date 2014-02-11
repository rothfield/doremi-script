#!/bin/bash

PID_FILE="comp.pid"
if [ -e $PID_FILE ] 
then
		COMPOJURE_PID=$(cat $PID_FILE)
		kill -9 $COMPOJURE_PID
		rm $PID_FILE
else
		echo "compojure is not running.."
fi



WATCHER_PID_FILE="watcher.pid"
if [ -e $WATCHER_PID_FILE ] 
then
		WATCHER_PID=$(cat $WATCHER_PID_FILE)
		kill -9 $WATCHER_PID
		rm $WATCHER_PID_FILE
else
		echo "watcher is not running.."
fi






