#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = ""
composer = ""
  tagline = ""  % removed 
}
%{
 Mode: aeolian

| gm Pg -S -- |
el-ean-or rig-by 
 %}
  
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \aeolian
\autoBeamOn  
\cadenzaOn
\bar "|" ef'8[ f'8] g'8[ ef'8]~ ef'8[ c'8]~ c'4  \bar "|"
}

text = \lyricmode {
  el- ean- or rig- by
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
