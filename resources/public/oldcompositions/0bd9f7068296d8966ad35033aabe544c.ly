#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
SSSS---RRR ggggg

m
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  \times 8/10{ c'32[ c'32 c'32 c'8 d'32 d'32 d'32] }  \times 4/5{ ef'16[ ef'16 ef'16 ef'16 ef'16] }  \bar "" \break 
  f'4 \bar "" \break 
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