#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
[I]       [V7]      [iii]
S - - - | R - - - | G - - - | m - - - | 

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  c'4^"I" r4 r4 r4 \bar "|"  d'4^"V7" r4 r4 r4 \bar "|"  e'4^"iii" r4 r4 r4 \bar "|"  f'4 r4 r4 r4 \bar "|" \bar "" \break 
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