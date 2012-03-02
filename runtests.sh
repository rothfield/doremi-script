# Run tests using nodeunit
mkdir -p tmp
nodeunit lib/doremi-script-base/parser_test.js
nodeunit lib/doremi-script-base/fractions_test.js
nodeunit lib/doremi-script-base/to_lilypond_test.js
#nodeunit lib/doremi-script/to_musicxml_test.js
nodeunit lib/doremi-script-base/to_html_test.js
nodeunit lib/doremi-script-base/tree_iterators_test.js
echo "testing command line utility doremi2htmldoc with input | SRG"
echo "| SRG" | doremi2htmldoc | tee > tmp/test2htmldoc.html ;  
echo "`wc -l tmp/test2htmldoc.html`"
echo "testing command line utility doremi2ly with input | SRG"
echo "| SRG" | doremi2ly | tee > tmp/doremi2ly.out ;  
#echo "lines in tmp/doremi2ly.out is `wc -l tmp/doremi2ly.out`"
echo "`wc -l tmp/doremi2ly.out`"
echo "testing command line utility doremiparse with input | SRG"
echo "| SRG" | doremiparse | tee > tmp/doremiparse.out ; 
echo "`wc -l tmp/doremiparse.out`"
echo "SRG\n|  m" | doremi2musicxml | tee > tmp/doremi2musicxml.xml; musicxml2ly tmp/doremi2musicxml.xml -o tmp/doremi2musicxml.xml.ly 
echo "`wc -l tmp/doremi2musicxml.xml.ly`"

cat lib/doremi-script-base/test_files/bansuri.txt | doremi2musicxml | tee > tmp/bansuri.xml
echo "`wc -l tmp/bansuri.xml`"

