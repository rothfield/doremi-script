# Run tests using nodeunit
mkdir -p tmp
nodeunit lib/doremi-script/parser_test.js
nodeunit lib/doremi-script/fractions_test.js
nodeunit lib/doremi-script/to_lilypond_test.js
nodeunit lib/doremi-script/to_html_test.js
nodeunit lib/doremi-script/tree_iterators_test.js
echo "testing command line utility doremi2htmldoc with input | SRG"
echo "| SRG" | doremi2htmldoc > tmp/test2htmldoc.html ;  
echo "`wc -l tmp/test2htmldoc.html`"
echo "testing command line utility doremi2ly with input | SRG"
echo "| SRG" | doremi2ly > tmp/doremi2ly.out ;  
#echo "lines in tmp/doremi2ly.out is `wc -l tmp/doremi2ly.out`"
echo "`wc -l tmp/doremi2ly.out`"
echo "testing command line utility doremiparse with input | SRG"
echo "| SRG" | doremiparse > tmp/doremiparse.out ; 
echo "`wc -l tmp/doremiparse.out`"

