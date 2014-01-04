#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = ""
composer = ""
  tagline = ""  % removed 
}
%{
 |: S - - - | G - - - :|

|: R - - -  | m - - - :| 
 %}
  
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn  
\cadenzaOn
\bar "|:"  c'4~ c'4~ c'4~ c'4 \bar "|"  e'4~ e'4~ e'4~ e'4 \bar ":|"\break        \grace s64 
\bar "|:"  d'4~ d'4~ d'4~ d'4 \bar "|"  f'4~ f'4~ f'4~ f'4 \bar ":|"
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
