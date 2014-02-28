#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
hi john

[Dm6]             [Cm6]
1 2 3 4 5 77777 | 444 5555 666 |
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  c'4^"Dm6" d'4 e'4 f'4 g'4 \times 4/5{ b'16[ b'16 b'16 b'16 b'16] }  \bar "|"  \times 2/3{ f'8[^"Cm6" f'8 f'8] }  g'16[ g'16 g'16 g'16] \times 2/3{ a'8[ a'8 a'8] }  \bar "|" \bar "" \break 
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