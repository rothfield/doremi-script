#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = ""
composer = ""
}
%{
TimeSignature: 3/4

| S - - | R - S | N - - | - - - |
                  .
  i-      rene good night

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn
\cadenzaOn


 \bar "|"   c'4 r4 r4 \bar "|"   d'4 r4  c'4 \bar "|"   b4 r4 r4 \bar "|"  r4 r4 r4 \bar "|" 
}


text = \lyricmode {
i- rene good night
}

\score{
\transpose c' d'
<<
\new Voice = "one" {
\melody
}
\new Lyrics \lyricsto "one" \text
>>
\layout {
\context {
\Score
\remove "Bar_number_engraver"
}
}
\midi {
\context {
\Score
tempoWholesPerMinute = #(ly:make-moment 200 4)
}
}
}