#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{


}
%{
PgPgmgrS
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn
\cadenzaOn


 g'32 ef'32 g'32 ef'32 f'32 ef'32 df'32 c'32 \break

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