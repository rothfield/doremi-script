#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = "Bansuri"
composer = "Traditional"
}
%{

%}
melody = {
\time 4/4
\clef treble
\key c \phrygian
\autoBeamOn
\cadenzaOn


 \bar "|:"  c'8( df'8 \bar "|"  \grace {c'16}bf4) c'4 ef'8( f'8 \afterGrace g'8{bf'16} af'8) \bar "|"  g'4^"i" r4 g'4 g'4 \bar "|"  g'4^"IV" r4 a'4 \afterGrace bf'4({a'16[ c''16 bf'16 a'16 bf'16])} \bar "|"  \break
\grace s64 g'4 af'4\mordent g'4 af'4 \bar "|"  g'8( f'8 g'16 f'16 bf'16 g'16) \grace {f'16[ ef'16 f'16]}ef'4( f'4) \bar "|"  g'16( af'16 bf'16 c''16) r4 ef''4 c''4 \bar "|"  \break
\grace s64 af'8( g'16 f'16 ef'4) g'4 \afterGrace f'4({g'16[ f'16])} \bar "|"  df'4\mordent r4 c'4 \bar ":|"  \break
\grace s64 ef'4 f'4 af'4( bf'4) \bar "|"  c''4 r4 \grace {ef''16}df''4 c''4 \bar "|"  \break

}


text = \lyricmode {
ban-   su- ri    ba- ja ra- hi dhu- na ma- dhu- ra kan- nai-      ya  khe-    la- ta ga-    wa- ta ho- ri ji- na ja-  u san- ga 
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