upper dot nodes for this ornament
[{:group_line_no 1, :my_type :upper_octave_dot, :start_index 23}]
upper dot nodes for this ornament
[]
upper dot nodes for this ornament
[]
upper dot nodes for this ornament
[{:group_line_no 1, :my_type :upper_octave_dot, :start_index 26}]
upper dot nodes for this ornament
[]
#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = "hi"
composer = ""
  tagline = ""  % removed 
}
%{
 Title: hi

   NRSNS
   .  .
| S
 
 %}
  
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn  
\cadenzaOn
\bar "|"  \afterGrace c'4 { b'32[ d'32 c'32 b'32 c'32] } \break

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
