#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = ""
composer = ""
}
%{
| S-R ---

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn
\cadenzaOn


 \bar "|"  \times 2/3{ c'4 d'8 }  \times 2/3{ r4 } 
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