#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{


}
%{

%}
melody = {
\clef treble
\key c \major
\cadenzaOn


  \times 16/27{ c'64[ c'64 c'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64 df'64] }   
 
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