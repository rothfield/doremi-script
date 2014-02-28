#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
1) | SSSSS RR | mmmm mmmm P  ddddd nnnnn

SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS

RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRrrrrrr
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  \times 4/5{ c'16[ c'16 c'16 c'16 c'16] }  d'8[ d'8] \bar "|"  f'16[ f'16 f'16 f'16] f'16[ f'16 f'16 f'16] g'4 \times 4/5{ af'16[ af'16 af'16 af'16 af'16] }  \times 4/5{ bf'16[ bf'16 bf'16 bf'16 bf'16] }  \bar "" \break 
  \times 32/70{ c'128[ c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128 c'128] }  \bar "" \break 
  \times 32/50{ d'128[ d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 d'128 df'128 df'128 df'128 df'128 df'128 df'128] }  \bar "" \break 
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