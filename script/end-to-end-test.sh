fname=./tmp/$(basename $1)
echo "Creating ./tmp directory if it doesn't exist"
`mkdir -p tmp`
echo "Deleting $fname.json $fname.ly $fname.png $fname.mid $fname.ps if they exist"
rm -f $fname.json $fname.ly $fname.png $fname.mid $fname.ps
echo "Running clojure parser (instaparse) to create $fname.json file. Then feeding it into doremi_json_to_lilypond from doremi-script-base and lilypond"
cat $1 | lein run > $fname.json ; cat $fname.json | doremi_json_to_lilypond > $fname.ly ; lilypond -o $fname --png $fname.ly ; feh $fname.png

