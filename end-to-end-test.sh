#rm -f tmp.png ; echo "|: S--g | rr RR RG :|" | lein run | to_lilypond > tmp.ly ; lilypond --png tmp.ly ; feh tmp.png
# rm -f tmp.png ; echo "|: S--S--S- :|" | lein run > tmp2.txt ; cat tmp2.txt | to_lilypond > tmp.ly ; lilypond --png tmp.ly ; feh tmp.png
rm -f tmp.png ; echo "|S" | lein run > tmp2.txt ; cat tmp2.txt | to_lilypond > tmp.ly ; lilypond --png tmp.ly ; feh tmp.png


