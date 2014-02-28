#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
SS   rrrrrrrrrrrrrr
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  c'8[ c'8] \times 8/14{ df'32[ df'32 df'32 df'32 df'32 df'32 df'32 df'32 df'32 df'32 df'32 df'32 df'32 df'32] }  \bar "" \break 
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