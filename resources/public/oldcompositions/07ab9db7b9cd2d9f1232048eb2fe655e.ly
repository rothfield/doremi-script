#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
.
RPS           gggggggggggggggggggggggggggggggggggggggggg |
  .
  aaaaaaaaa bbbbbbb ccccccccc


%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  \times 2/3{ d''8[ g'8 c8] }  \times 32/42{ ef'128[ ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128 ef'128] }  \bar "|" \bar "" \break 
 }
text = \lyricmode {
aaaaaaaaa bbbbbbb ccccccccc                                           
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