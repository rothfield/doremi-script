#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
mmmmmmmmmmmPg
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  \times 8/13{ f'32[ f'32 f'32 f'32 f'32 f'32 f'32 f'32 f'32 f'32 f'32 g'32 ef'32] }  \bar "" \break 
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