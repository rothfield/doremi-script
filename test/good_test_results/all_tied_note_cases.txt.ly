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


  r4 c'8[ c'8] \bar "|"  
 \grace s64   r4 r8 c'8 \bar "|"  
 \grace s64   c'4 r4 r4 r4 \bar "|"  
 \grace s64   c'4 r8 d'8 \bar "|"  
 \grace s64   c'4 r4 r8 d'8 \bar "|"  
 \grace s64   c'4 r4 r4 d'4 r4 r4 \bar "|"  
 \grace s64   c'4 r4 r4 r4 \bar "|"  d'4 r4 r4 r4 \bar "|"  
 \grace s64   c'4 r4 r4 r4 \bar "|"  r4 r4 r4 r4 \bar "|"  
 \grace s64   r8 c'8 r4 d'4 r8 e'8 \bar "|"  r4 f'4 r8 g'8 r4 \bar "|"  
 
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