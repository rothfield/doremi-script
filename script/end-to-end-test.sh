fname=./tmp/$(basename $1)
echo "Creating ./tmp directory if it doesn't exist"
`mkdir -p tmp`
echo "Deleting $fname.json $fname.ly $fname.png $fname.mid $fname.ps if they exist"
rm -f $fname.json $fname.ly $fname.png $fname.mid $fname.ps
echo "Running clojure parser (instaparse) to create $fname.json file. Then feeding it into doremi_json_to_lilypond from doremi-script-base and lilypond"
cat $1 | lein run > $fname.json ;
# I've been unable to get clojure to pretty-print the json, so use node.
node -e "console.log(JSON.stringify(JSON.parse(require('fs') \
		      .readFileSync(process.argv[1])), null, 4));" $fname.json > $fname.json.pretty
cat $fname.json.pretty | doremi_json_to_lilypond > $fname.ly ; lilypond -o $fname --png $fname.ly ; feh $fname.png


