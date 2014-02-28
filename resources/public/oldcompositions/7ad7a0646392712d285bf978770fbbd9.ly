#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
SSSS rrrr gggg mmmm RRRR | gggg rrrr SSSS ----
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  c'16[ c'16 c'16 c'16] df'16[ df'16 df'16 df'16] ef'16[ ef'16 ef'16 ef'16] f'16[ f'16 f'16 f'16] d'16[ d'16 d'16 d'16] \bar "|"  ef'16[ ef'16 ef'16 ef'16] df'16[ df'16 df'16 df'16] c'16[ c'16 c'16 c'16] r4 }
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