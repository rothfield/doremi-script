#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = ""
composer = ""
  tagline = ""  % removed 
}
%{
 Key: D
Mode: phrygian          

                                i            IV         . 
         3              n       +            2         DSnDn
1)|: (Sr | n) S   (gm <P  d)> | P - P  P   | P - D    n     |
           .
      ban-    su-  ri          ba- ja ra-   hi  dhu- na 
 %}
  
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \phrygian
\autoBeamOn  
\cadenzaOn
\bar "|:" c'8[(  df'8]  \bar "|" bf4)  c'4  ef'8[(  f'8]  \afterGrace g'8[ { bf'32 }  af'8])  \bar "|" g'2~^"i"   g'4  g'4  \bar "|" g'2~^"IV"   a'4  \afterGrace bf'4 { a'32[ c''32 bf'32 a'32 bf'32] }  \bar "|"
}

text = \lyricmode {
  ban- su- ri ba- ja ra- hi dhu- na
}

\score{
\transpose c' d'
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
