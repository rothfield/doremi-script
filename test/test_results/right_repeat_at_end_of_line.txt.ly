#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = ""
composer = ""
}
%{
|: S - - - | G - - - :|

|: R - - -  | m - - - :|

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn
\cadenzaOn


 \bar "|:"   c'4 r4 r4 r4 \bar "|"   e'4 r4 r4 r4 \bar ":|" 
 \bar "|:"   d'4 r4 r4 r4 \bar "|"   f'4 r4 r4 r4 \bar ":|" 

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