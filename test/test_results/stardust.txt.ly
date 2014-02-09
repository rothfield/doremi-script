#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = "Stardust"

}
%{
Title: Stardust
Key: C

  C   C7       F                   Fm        Bb7
         ..    . .             .   .         ..      .
| - - -N SS# | R S D m | R m D G | G - - - | RS dm R R |

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn
\cadenzaOn


 \bar "|"  r4^"C" r4 r8^"C7" b'8 c''8 cs''8 \bar "|"  d''4^"F" c''4 a'4 f'4 \bar "|"  d'4 f'4 a'4 e''4 \bar "|"  e''4^"Fm" r4 r4 r4 \bar "|"  d''8^"Bb7" c''8 af'8 f'8 d'4 d''4 \bar "|"  \break

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