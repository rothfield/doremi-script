# Run tests using nodeunit
nodeunit webapp/doremi_web/public/js/parser_test.js
nodeunit webapp/doremi_web/public/js/fractions_test.js
nodeunit webapp/doremi_web/public/js/to_lilypond_test.js
echo "testing command line utility sargam2htmldoc with input | SRG"
echo "| SRG" | sargam2htmldoc > tmp/test2htmldoc.html ;  
echo "`wc -l tmp/test2htmldoc.html`"
echo "testing command line utility sargam2ly with input | SRG"
echo "| SRG" | sargam2ly > tmp/sargam2ly.out ;  
#echo "lines in tmp/sargam2ly.out is `wc -l tmp/sargam2ly.out`"
echo "`wc -l tmp/sargam2ly.out`"
echo "testing command line utility sargamparse with input | SRG"
echo "| SRG" | sargamparse > tmp/sargamparse.out ; 
echo "`wc -l tmp/sargamparse.out`"

