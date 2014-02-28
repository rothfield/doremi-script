#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 

title = "Yesterday in numbers"}
%{
Title: Yesterday in numbers

                         .
| 21 1 - - | -- 34# 5#6 71 | 7 66 -- -- |  
yes-ter-day all my trou-bles seemed so far away 
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  d'8[ c'8] c'4 r4 r4 \bar "|"  r4 e'8[ fs'8] gs'8[ a'8] b'8[ c''8] \bar "|"  b'4 a'8[ a'8] r4 r4 \bar "|" \break 
 }
text = \lyricmode {
yes- ter- day all my trou- bles seemed so far away  
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