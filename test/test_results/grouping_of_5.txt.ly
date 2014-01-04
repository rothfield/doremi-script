#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = ""
composer = ""
  tagline = ""  % removed 
}
%{
                                        .
S----R-- --G----m ----P--- -D----N- ---S---- 
 %}
  
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn  
\cadenzaOn
 c'8[~c'32[     d'16.]~   d'16[  e'8~e'32     f'32]~ f'8[    g'8]~    g'32[ a'8~a'32     b'16]~  b'16.[   c''8]~c''32]     \break

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
