#!/bin/bash

# Process all txt files in the fixtures directory. 
# Generates a report showing doremi source, generated jpg, json , and lilypond source
#
# I hack away the report manually here.
echo "WARNING: lily2image fails for long images"
DIR=`dirname $0`  # directory of this script
target_directory=$DIR/../test/test_results
mkdir -p $target_directory
echo "Writing test results to  $target_directory"
CTR=1
css_file="$DIR/report.css"
cp $css_file $target_directory/report.css
cp $DIR/zepto.min.js  $target_directory/
cp $DIR/report.js  $target_directory/
report=$target_directory/report.html

echo "<html><head>" > $report # create new report
echo "<link rel='stylesheet' href='report.css'>" >> $report
echo "<title>doremi-script report: $argcount files tested</title>" >> $report
echo "</head>" >> $report
echo "<body>" >> $report
echo "<h2>doremi-script end to end test</h1>" >> $report
echo "<h3>`date`</h3>" >> $report
list=($1)
argcount="${#list[@]}"
echo "list is ${list[@]}" 
for ARG in "${list[@]}"; do
		echo $ARG
		echo "removing json ly etc files"
		mybasename=$(basename $ARG)
		fname=$target_directory/$mybasename
		rm -f $fname.json $fname.ly $fname.png $fname.mid $fname.ps $fname.jpeg
done


echo "Running lein run --test-all to create lilypond files"
lein run --test-all $1
echo "done creating lilypond files"
echo "count is $argcount"
for ARG in $1; do
		if [[ ! -f $ARG ]] ; then
				    echo 'File "$ARG" is not there, aborting.'
						    exit
						fi
	  echo "$ARG: 	File # $CTR/$argcount"
		mybasename=$(basename $ARG)
		fname=$target_directory/$mybasename
		echo "<hr>" >> $report 
		echo "<h2>$mybasename</h2>" >> $report
		echo "<pre class="doremi-source">" >> $report
		cat $ARG | sed -f $DIR/sed_snippet.txt >> $report	
		echo "</pre>" >> $report
		echo "<button  class="show_ly" data-which='$CTR'>Show Lilypond Source</button>" >> $report
		echo "<pre id='ly$CTR' class='ly-data'>" >> $report
		cat $fname.ly | sed -f $DIR/sed_snippet.txt >> $report	
		echo "</pre>" >> $report
	 	lily2image -q -r=72 -f=png $fname 2>&1  
		rm -f $fname.ps
		echo "<div><img src='$mybasename.png'></div>" >> $report
		CTR=$((CTR + 1))
done

echo "<script src='zepto.min.js'></script><script src='report.js'></script></body></html>" >> $report

echo "Report is at $report"
chromium $report
