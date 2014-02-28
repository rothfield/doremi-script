#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
| - - PP | D P S | N
      ..   ..      .
ha-ppy birth-day to you
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  r4 r4 g8[ g8] \bar "|"  a4 g'4 c'4 \bar "|"  b4 \break 
 }
text = \lyricmode {
ha- ppy birth- day to you 
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