#!/bin/bash

time {
		tempdir=$HOME/tmp
		fname=$tempdir/$(basename $1)
		dirname=`dirname $0`
		mkdir -p $tempdir
		cp $1 $tempdir
		#cat $1 | lein run --json > $fname.json
		echo "lein run --ly ..."
		cat $1 | lein run --ly >  $fname.ly 
		echo "lily2image..."
		lily2image -r=72 -f=png $fname 2>&1  
		rm -f $fname.mid $fname.ps
}
feh $fname.png 

