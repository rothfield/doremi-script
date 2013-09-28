#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 


tagline = ""  % removed 
}
%{
  Fm7
| S
  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
\partial 4*1  | c'4^"Fm7" \break

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
  \remove "Bar_number_engraver"
} 
}
\midi { }
}
