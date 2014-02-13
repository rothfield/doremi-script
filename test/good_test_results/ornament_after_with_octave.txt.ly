#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = "hi"

}
%{

%}
melody = {
\clef treble
\key c \major
\cadenzaOn


  \afterGrace c'4({b16[ d'16 c'16 b16 c'16])}  
 
}


text = \lyricmode {
 
}

\score{

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