#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = "Yesterday"
composer = "McCartney"
}
%{

%}
melody = {
\clef treble
\key c \major
\cadenzaOn


  d'8[ c'8] c'4 r4 r4 \bar "|"  
 
}


text = \lyricmode {
   
}

\score{
\transpose c' f'
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