#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
S

r

gmmmmmmmmmmmmmmmmm
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  c'4 \bar "" \break 
  df'4 \bar "" \break 
  \times 16/18{ ef'64[ f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64] }  \bar "" \break 
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