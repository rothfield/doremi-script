#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = ""
composer = ""
}
%{
    Pm
 P m | r - S
 wa-ta ho-ri

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn
\cadenzaOn


 g'4 f'4 \bar "|"  df'4 r4 c'4

}


text = \lyricmode {
wa- ri ta ho-
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