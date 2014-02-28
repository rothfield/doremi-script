#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
SSSSSSSSSS | RRRR ---------
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  \times 8/10{ c'32[ c'32 c'32 c'32 c'32 c'32 c'32 c'32 c'32 c'32] }  \bar "|"  d'16[ d'16 d'16 d'16] \times 8/9{ r4 }  }
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