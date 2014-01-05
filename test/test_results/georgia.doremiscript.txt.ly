#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = "Georgia"
composer = "Hoargy Carmichael"
  tagline = ""  % removed 
}
%{
 Title:Georgia
Author:Hoargy Carmichael

     Georgia Georgia
     No peace I find

  +         2  
| GP - -  - | GR - - - | - G D G | R - - SR |
  Geor-gia geor-gia no peace I find      just an   

 
  
  
 
 %}
  
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn  
\cadenzaOn
\bar "|" e'8[ g'8]~ g'4~ g'4~ g'4 \bar "|" e'8[ d'8]~ d'4~ d'4~ d'4~ \bar "|" d'4 e'4 a'4 e'4 \bar "|" d'4~ d'4~ d'4 c'8[ d'8] \bar "|"
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
