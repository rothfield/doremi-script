#!/bin/bash

echo "Warning: This script will kill all java and inotifywait process"
echo "It uses killall -9 java and killall -9 inotifywait  !!!"
read -p "Are you sure? " -n 1 -r
echo    # (optional) move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
		    # do dangerous stuff
		echo "Killing java and inotifywait"
killall -9 java
killall -9 inotifywait
fi

