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


  c'4 c'4 \times 2/3{ c'4[ c'8] }  \times 2/3{ c'8[ c'8 c'8] }  c'16[ c'16 c'16 c'16] \times 4/7{ c'8.[ d'8. e'16] }  r16 c'16 d'16 e'16 r32 c'16 d'32 e'32 f'32 g'16  
 
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