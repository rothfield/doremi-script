#rm -f tmp.png ; echo "|: S--g | rr RR RG :|" | lein run | to_lilypond > tmp.ly ; lilypond --png tmp.ly ; feh tmp.png
# rm -f tmp.png ; echo "|: S--S--S- :|" | lein run > tmp2.txt ; cat tmp2.txt | to_lilypond > tmp.ly ; lilypond --png tmp.ly ; feh tmp.png
#rm -f tmp.png ; echo ":\n~\nS\nHi" | lein run > tmp2.txt ; cat tmp2.txt | to_lilypond > tmp.ly ; lilypond --png tmp.ly ; feh tmp.png

#echo $1
fname=$HOME/tmp/tmpv1/$(basename $1)
`mkdir -p $HOME/tmp/tmpv1`
rm -f $fname.json $fname.ly $fname.png $fname.mid $fname.ps
echo $fname
cat $1 | doremiparse > $fname.json ; cat $fname.json | doremi_json_to_lilypond > $fname.ly ; lilypond -o $fname --png $fname.ly ; feh $fname.png

