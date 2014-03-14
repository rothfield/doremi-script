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


  g'8[( e'8]) \bar "|"  c'4 e'4 g'4 \bar "|"  c''4 r4 e''8[ d''8] \bar "|"  
 
}


text = \lyricmode {
Oh  say can you see by the 
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