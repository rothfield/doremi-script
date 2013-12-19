#!/bin/bash

tempdir=$HOME/tmp/all-end-to-end
dirname=`dirname $0`
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "Using tmp directory $tempdir"
echo "Creating  directory if it doesn't exist"
argcount=$#
COUNTER=1
mkdir -p $tempdir
css_file="$dirname/report.css"
cp $css_file $tempdir/report.css
cp $DIR/zepto.min.js  $tempdir/
cp $DIR/report.js  $tempdir/
echo "Using css file $css_file"
report=$HOME/tmp/all-end-to-end/report.html
echo "<html><head><link rel='stylesheet'  href='report.css'><title>doremi-script report: $argcount files tested</title><body><h1>doremi-script end to end test</h1>" > $report
for ARG in "$@"; do
		echo "Copying $ARG to $tempdir"
		cp $ARG $tempdir
		fname=$tempdir/$(basename $ARG)
		mybasename=$(basename $ARG)
		echo "<br/><hr>" >> $report 
		echo "<h2>$(basename $ARG)</h2>" >> $report
		echo "<pre>" >> $report
		cat $ARG | sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g; s/"/\&quot;/g; s/'"'"'/\&#39;/g' >> $report	
		echo "</pre>" >> $report
	  echo "	File # $COUNTER/$argcount"
		echo "Deleting $fname.json $fname.ly $fname.png $fname.mid $fname.ps if they exist"
		rm -f $fname.json $fname.ly $fname.png $fname.mid $fname.ps
		echo "Reading $ARG"
		echo "Temporary files created are $fname.json and $fname.ly"
		echo "creating $fname.json"
		echo "ARG is $ARG"
		cat $fname | lein run --json > $fname.json
		echo "using lein run --ly"

	#			result2= `#{settings.lily2image} -r=72 -f=jpg #{fp}.ly 2>&1`  
	  echo "fname.ly is $fname.ly"
		cat $ARG | lein run --ly >  $fname.ly 
		#zz= "onclick=\"$('#ly$COUNTER').toggle()\""
		echo "<button  class="show_ly" data-which='$COUNTER'>Show Lilypond Source</button><button class="show_json" data-which='$COUNTER'>Show json</button><pre id='ly$COUNTER' class='ly-data'>" >> $report
		cat $fname.ly | sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g; s/"/\&quot;/g; s/'"'"'/\&#39;/g' >> $report	
		echo "</pre>" >> $report

		echo "Processing $ARG"
		echo "WARNING: lily2image fails for long images"
	 	lily2image -r=72 -f=jpeg $fname 2>&1  
		rm -f $fname.mid $fname.ps
		echo "<div><img src='$mybasename.jpeg'></div>" >> $report
		COUNTER=$((COUNTER + 1))
done

echo "<script src='zepto.min.js'></script><script src='report.js'></script></body></html>" >> $report
