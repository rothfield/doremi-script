#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
Key: E
Mode: Major

| P G G RS | R m m - | PG mR P-RG mR |
  .                          .

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
  g4 e'4 e'4 d'8[ c'8] \bar "|"  d'4 f'4 f'4 r4 \bar "|"  g'8[ e'8] f'8[ d'8] g8[ d'16 e'16] f'8[ d'8] \bar "|" \bar "" \break 
 }
text = \lyricmode {
                 
}
\score{
\transpose c' e'
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