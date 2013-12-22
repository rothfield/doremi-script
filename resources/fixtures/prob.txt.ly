#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"

\score{

<<
  \new Voice = "one" {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn  
\bar "|:"  c' d' e' f' :| \break 

  c' d' e' f' \bar "|" \break
  }
>>
 }
