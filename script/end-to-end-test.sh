fname=$HOME/tmp/$(basename $1)
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
dirname=`dirname $0`
echo "dirname is $dirname"
echo "Creating ./tmp directory if it doesn't exist"
mkdir -p $HOME/tmp
echo "dirname is $dirname"
echo "Deleting $fname.json $fname.ly $fname.png $fname.mid $fname.ps if they exist"
rm -f $fname.json $fname.ly $fname.png $fname.mid $fname.ps
echo "Running clojure parser (instaparse) to create $fname.json file. Then feeding it into doremi_json_to_lilypond from doremi-script-base and lilypond"
#cat $1 | lein run > $fname.json ;
# I've been unable to get clojure to pretty-print the json, so use node.
#cat $fname.json | doremi_json_to_lilypond > $fname.ly ; lilypond -o $fname --png $fname.ly ; feh $fname.png
#cat $1 | java -jar $dirname/../target/doremi-script-standalone.jar > $fname.json ; cat $fname.json | doremi_json_to_lilypond > $fname.ly ; lilypond -o $fname --png $fname.ly ; feh $fname.png
 cat $1 | java -jar $dirname/../target/doremi-script-standalone.jar | tee $fname.json | doremi_json_to_lilypond > $fname.ly ;  lilypond -o $fname --png $fname.ly ; feh $fname.png


