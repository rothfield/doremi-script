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
\autoBeamOn
\cadenzaOn


 r4 a'8 a'8 g'8 f'8 e'8 d'8 \bar "|"  f'4 e'8 e'8 r4 d'4 \bar "|"  c'4 e'4 d'4 a4 \bar "|"  c'4 e'8 e'8 r4 r4 \bar "||"   \break

}


text = \lyricmode {
now it looks as though they're here to stay oh I be- lieve in Yes- ter- day 
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