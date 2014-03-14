#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{


}
%{

%}
melody = {
\clef treble
\key c \aeolian
\cadenzaOn


  ef'8[ f'8] g'8[ ef'8] r8 c'8 r4 \bar "|"  
 
}


text = \lyricmode {
el- ean- or rig- by 
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
}
}
\midi {
\context {
\Score
tempoWholesPerMinute = #(ly:make-moment 200 4)
}
}
}