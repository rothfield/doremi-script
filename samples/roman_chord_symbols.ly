#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{   }
\include "english.ly"
%{
Filename: roman_chord_symbols

  I    IV   III  V7
| SGRG RmGm GPmP RmGm |
  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
\bar "|"  c'16^"I" e'16 d'16 e'16 d'16^"IV" f'16 e'16 f'16 e'16^"III" g'16 f'16 g'16 d'16^"V7" f'16 e'16 f'16 \bar "|"  \break

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
\layout { }
\midi { }
}