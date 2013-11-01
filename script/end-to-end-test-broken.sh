#rm -f tmp.png ; echo "|: S--g | rr RR RG :|" | lein run | to_lilypond > tmp.ly ; lilypond --png tmp.ly ; feh tmp.png
# rm -f tmp.png ; echo "|: S--S--S- :|" | lein run > tmp2.txt ; cat tmp2.txt | to_lilypond > tmp.ly ; lilypond --png tmp.ly ; feh tmp.png
#rm -f tmp.png ; echo ":\n~\nS\nHi" | lein run > tmp2.txt ; cat tmp2.txt | to_lilypond > tmp.ly ; lilypond --png tmp.ly ; feh tmp.png

#echo $1
fname=./tmp/$(basename $1)
`mkdir -p tmp`
rm -f $fname.json $fname.ly $fname.png $fname.mid $fname.ps
echo $fname
cat $1 | lein run > $fname.json ; cat $fname.json | doremi2ly > $fname.ly ; lilypond -o $fname --png $fname.ly ; feh $fname.png
#cat ./resources/fixtures/yesterday.txt | lein run > tmp2.txt ; cat tmp2.txt | to_lilypond > tmp.ly ; lilypond --png tmp.ly ; feh tmp.png

