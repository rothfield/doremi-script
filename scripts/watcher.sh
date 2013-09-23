#!/bin/sh
while inotifywait -e modify -e create -e delete /home/john/instatest/*; do
		   lein test
		done
