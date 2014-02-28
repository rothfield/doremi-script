#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
ggggggggggggggggggggggggggggggggggggggggggggrrrrrrrrr
mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  \times 32/50{ f'128[ f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128 f'128] }  \bar "" \break 
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