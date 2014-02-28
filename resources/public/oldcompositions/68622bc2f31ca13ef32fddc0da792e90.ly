#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{


}
%{

%}
melody = {
\clef treble
\key c \major
\cadenzaOn


  \times 32/76{ c'128[ c'128 c'128 df'128 df'128 df'128 df'128 df'128 df'128 df'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 g'128 g'128 g'128 g'128 g'128 g'128 g'128 g'128 g'128 g'128 g'128 g'128 g'128 g'128 g'128 g'128 g'128 g'128] }   
 
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
}
}
\midi {
\context {
\Score
tempoWholesPerMinute = #(ly:make-moment 200 4)
}
}
}