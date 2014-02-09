#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{


}
%{
gmgrS
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn
\cadenzaOn


 \times 4/5{ ef'16 f'16 ef'16 df'16 c'16 }  \break

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