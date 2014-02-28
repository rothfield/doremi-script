#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
g--mmmm |mmmmmmmmmmm
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  \times 4/7{ ef'8.[ f'16 f'16 f'16 f'16] }  \bar "|"  \times 8/11{ f'32[ f'32 f'32 f'32 f'32 f'32 f'32 f'32 f'32 f'32 f'32] }  \bar "" \break 
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