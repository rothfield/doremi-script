#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
S--M ggggggg mmmm P
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  c'8.[ fs'16] \times 4/7{ ef'16[ ef'16 ef'16 ef'16 ef'16 ef'16 ef'16] }  f'16[ f'16 f'16 f'16] g'4  
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