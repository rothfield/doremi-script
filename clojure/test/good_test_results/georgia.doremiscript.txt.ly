#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = "Georgia"
composer = "Hoargy Carmichael"
}
%{

%}
melody = {
\clef treble
\key c \major
\cadenzaOn


  e'8[ g'8] r4 r4 r4 \bar "|"  e'8[ d'8] r4 r4 r4 \bar "|"  r4 e'4 a'4 e'4 \bar "|"  d'4 r4 r4 c'8[ d'8] \bar "|"  
 
}


text = \lyricmode {
Geor- gia geor- gia no peace I find just an 
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