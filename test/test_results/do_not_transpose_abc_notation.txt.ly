#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
Key: F#
Mode: Minor

   .  .
A# C# F#

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key fs \minor
\cadenzaOn
  as'4 cs''4 fs''4 \bar "" \break 
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