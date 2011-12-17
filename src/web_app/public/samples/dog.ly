#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{   }
\include "english.ly"
%{
Filename: dog

| S S S -  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
\bar "|"  c'4 c'4 c'4~ c'4 \break

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
\layout { }
\midi { }
}