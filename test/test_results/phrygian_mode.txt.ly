#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{


}
%{

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \phrygian
\autoBeamOn
\cadenzaOn


 c'4 af'4 g'4 af'4 \bar "|"  f'4 g'4 ef'4 f'4 \bar "|"  bf'4 af'4 r4 df'4 \bar "|"  r4 ef'4 f'4 g'4 \bar "|"  \break

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