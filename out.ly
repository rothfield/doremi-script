#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
tagline = ""  % removed 
}
%{
S-R |
he-llo
%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  \times 2/3{ c'4[ d'8] }  \bar "|" \break 
 }
text = \lyricmode {
he- llo 
}
\score{

<<
\new Voice = "one" {
\set midiInstrument = #"flute"
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
tempoWholesPerMinute = #(ly:make-moment 100 4)
}
}
}