#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{

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