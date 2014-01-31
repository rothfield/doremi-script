#!/bin/bash

target_directory=$HOME/tmp
mkdir -p $target_directory
echo "Writing test results to  $target_directory"
argcount=$#
CTR=1
for ARG in "$@"; do
		if [[ ! -f $ARG ]] ; then
				    echo 'File "$ARG" is not there, aborting.'
						    exit
						fi
		mybasename=$(basename $ARG)
		fname=$target_directory/$mybasename
	  echo "$ARG: 	File # $CTR/$argcount"
		cat $ARG | lein run --new > $fname.parsetree
		CTR=$((CTR + 1))
done
