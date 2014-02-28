#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
SSS | rrr ggg mmm PP DD 
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  \times 2/3{ c'8[ c'8 c'8] }  \bar "|"  \times 2/3{ df'8[ df'8 df'8] }  \times 2/3{ ef'8[ ef'8 ef'8] }  \times 2/3{ f'8[ f'8 f'8] }  g'8[ g'8] a'8[ a'8] \bar "" \break 
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