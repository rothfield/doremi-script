#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = ""
composer = ""
  tagline = ""  % removed 
}
%{
 TimeSignature: 3/4

| S - - | R - S | N - - | - - - |
                  .
  i-      rene good night 
 %}
  
melody = {
\time 3/4
\clef treble
\key c \major
\autoBeamOn  
\cadenzaOn
\bar "|" c'2.~    \bar "|" d'2~   c'4  \bar "|" b2.~    \bar "|" b2.~    \bar "|"
}

text = \lyricmode {
  i- rene good night
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
