#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = "Yesterday"
composer = "McCartney"
  tagline = ""  % removed 
}
%{
 Filename: yesterday
Key: F
Mode: major
Title: Yesterday
Author: McCartney
Time: 4/4
EnteredBy: John Rothfield

1) |  RS S -- --  |  
 %}
  
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn  
\cadenzaOn
\bar "|" d'8[ c'8] c'2. \bar "|"  \break        

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
