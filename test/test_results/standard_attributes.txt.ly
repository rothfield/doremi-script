#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = "Sargam"
composer = "Traditional"
}
%{

%}
melody = {
\time 3/4
\clef treble
\key c \phrygian
\autoBeamOn
\cadenzaOn


 \bar "|:"  r4 d'8 ef'8 r8 c'8 d'4 \bar "|"  ef'4 r4 r4 \bar "|"  r4 r4 \afterGrace ef'4{d'16[ f'16 ef'16 d'16 ef'16]} \bar "|"  \grace {ef'16}df'4 c'4 r4 \bar "|"  r4 \bar ":|"  \break

}


text = \lyricmode {
        
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