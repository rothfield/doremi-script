#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = "Bansuri"
composer = "Traditional"
}
%{

%}
melody = {
\time 4/4
\clef treble
\key c \phrygian
\cadenzaOn


 \bar "|:"  c'8[( df'8] \bar "|"  \grace {c'16}bf4)
 

}


text = \lyricmode {
              ma- dhu- ra kan- nai-      ya  khe-    la- ta ga-    wa- ta ho- ri ji- na ja-  u san- ga 
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
\remove "Bar_number_engraver"
}
}
\midi {
\context {
\Score
tempoWholesPerMinute = #(ly:make-moment 200 4)
}
}
}
