#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
hi there

S(SS)|
hi there how are you doing ddddd

SSS |
 
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  \times 2/3{ c'8[ c'8( c'8]) }  \bar "|" \bar "" \break 
  \times 2/3{ c'8[ c'8 c'8] }  \bar "|" \bar "" \break 
 }
text = \lyricmode {
hi there     
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