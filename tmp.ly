#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 


tagline = ""  % removed 
}
%{
:
~
S
Hi
  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
c'''4\mordent \break

}

text = \lyricmode {
Hi
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