#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{


}
%{

%}
melody = {
\clef treble
\key c \phrygian
\cadenzaOn


 \bar "|:"  c'8[( df'8] \bar "|"  bf4) c'4 ef'8[( f'8] \afterGrace g'8[{bf'16} af'8]) \bar "|"  g'4^"i" r4 g'4 g'4 \bar "|"  g'4^"IV" r4 a'4 \afterGrace bf'4({a'16[ c'16 bf'16 a'16 bf'16])} \bar "|"  
 
}


text = \lyricmode {
ban-   su- ri    ba- ja ra- hi dhu- na 
}

\score{
\transpose c' d'
<<
\new Voice = "one" {
\melody
}
\new Lyrics \lyricsto "one" \text
>>
\layout {
\context {
\Score
}
}
\midi {
\context {
\Score
tempoWholesPerMinute = #(ly:make-moment 200 4)
}
}
}