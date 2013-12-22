upper dot nodes for this ornament
[{:group_line_no 4,
  :my_type :upper_upper_octave_symbol,
  :start_index 8}
 {:group_line_no 3, :my_type :upper_octave_dot, :start_index 6}
 {:group_line_no 1, :my_type :upper_octave_dot, :start_index 2}
 {:group_line_no 0,
  :my_type :upper_upper_octave_symbol,
  :start_index 0}]
#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = ""
composer = ""
  tagline = ""  % removed 
}
%{
 :
.
N
.
:
 S 
 %}
  
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn  
\cadenzaOn
 \acciaccatura {b'32}c'4 \break

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
