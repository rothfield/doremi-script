#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = "Bansuri_V2"
composer = "Traditional"
tagline = ""  % removed 
}
%{
AAK  
%}
melody = {
\clef treble
\key c \phrygian
\time 4/4
\autoBeamOn  
\break
 \partial 4*1  \bar "|:"  c'8( df'8 | \acciaccatura {c'32}bf4) c'4 ef'8( f'8 g'8 \acciaccatura {bf'32}af'8) | g'4~^"i" g'4 g'4 g'4 | g'4~^"IV" g'4 a'4 \afterGrace bf'4 { a'32[ c''32 bf'32 a'32 bf'32] } | \partial 4*0  \break
 | g'4 af'4\mordent g'4 af'4 | g'8( f'8 g'16 f'16 bf'16 g'16) \acciaccatura {f'32[ ef'32 f'32]}ef'4( f'4) | g'16( af'16 bf'16 c''16~) c''4 ef''4 c''4 | \break
 | af'8( g'16 f'16 ef'4) g'4 \afterGrace f'4 { g'32[ f'32] } | \partial 4*3  df'4~\mordent df'4 c'4 \bar ":|"  \break

}

text = \lyricmode {
ban- su- ri  ba- ja  ra- hi  dhu- na  ma- dhu- ra  kan- nai- ya  khe- la- ta  ga- wa- ta  ho- ri
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
\midi { }
}