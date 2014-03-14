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


  d'8[ c'8] c'4 r4 r4 \bar "|"  r4 e'8[ fs'8] gs'8[ a'8] b'8[ c''8] \bar "|"  b'4 a'8[ a'8] r4 r4 \bar "|"  
 \grace s64   r4 a'8[ a'8] g'8[ f'8] e'8[ d'8] \bar "|"  f'4 e'8[ e'8] r4 d'4 \bar "|"  c'4 e'4 d'4 a4 \bar "|"  c'4 e'8[ e'8] r4 r4 \bar "||"   
 
}


text = \lyricmode {
yes- ter day all my trou- bles seemed so far a- way now it looks as though they're here to stay oh I be- lieve in Yes- ter- day 
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