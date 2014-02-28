#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
hi john

[Dm6]
1 2 3 4 5 77777|
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  c'4^"Dm6" d'4 e'4 f'4 g'4 \times 4/5{ b'16[ b'16 b'16 b'16 b'16] }  \bar "|" \bar "" \break 
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