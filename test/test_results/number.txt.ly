#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
                            . . . . . . .
1 2 3 4 5 6 7 1 2 3 4 5 6 7 1 2 3 4 5 6 7
. . . . . . .         

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  c4 d4 e4 f4 g4 a4 b4 c'4 d'4 e'4 f'4 g'4 a'4 b'4 c''4 d''4 e''4 f''4 g''4 a''4 b''4 \bar "" \break 
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