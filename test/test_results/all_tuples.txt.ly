#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{


}
%{
SS S-m S-G-- S-G | SRGm SRGmP S--R S------R  S---R


%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn
\cadenzaOn


 c'8 c'8 \times 2/3{ c'4 f'8 }  \times 4/5{ c'8 e'8. }  \times 2/3{ c'4 e'8 }  \bar "|"  c'16 d'16 e'16 f'16 \times 4/5{ c'16 d'16 e'16 f'16 g'16 }  c'8. d'16 c'8.. d'32 \times 4/5{ c'4 d'16 }  \break

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