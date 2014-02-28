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
\key c \major


 c'16[ df'16 d'16 ef'16] e'16[ f'16 fs'16 g'16] af'16[ a'16 bf'16 b'16] cs'16[ ds'16 es'16 fs'16] \bar "|"  \break
\grace s64 cs'16[ ds'16 es'16 fs'16] \break
\grace s64 fs'4 \break

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