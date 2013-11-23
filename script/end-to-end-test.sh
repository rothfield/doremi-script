tempdir=$HOME/tmp
fname=$tempdir/$(basename $1)
dirname=`dirname $0`
echo "Using tmp directory $tempdir"
echo "Creating  directory if it doesn't exist"
mkdir -p $tempdir
echo "Deleting $fname.json $fname.ly $fname.png $fname.mid $fname.ps if they exist"
rm -f $fname.json $fname.ly $fname.png $fname.mid $fname.ps
echo "Reading $1"
echo "Temporary files created are $fname.json and $fname.ly"
cat $1 | java -jar $dirname/../target/doremi-script-standalone.jar | tee $fname.json | doremi_json_to_lilypond | tee $fname.ly |  lilypond -o $fname -f png - ; feh $fname.png

#cat $1 | lein run > $fname.json ;
# I've been unable to get clojure to pretty-print the json, so use node.
#cat $fname.json | doremi_json_to_lilypond > $fname.ly ; lilypond -o $fname --png $fname.ly ; feh $fname.png
#cat $1 | java -jar $dirname/../target/doremi-script-standalone.jar > $fname.json ; cat $fname.json | doremi_json_to_lilypond > $fname.ly ; lilypond -o $fname --png $fname.ly ; feh $fname.png
# works. cat $1 | java -jar $dirname/../target/doremi-script-standalone.jar | tee $fname.json | doremi_json_to_lilypond > $fname.ly ;  lilypond -o $fname --png $fname.ly ; feh $fname.png


