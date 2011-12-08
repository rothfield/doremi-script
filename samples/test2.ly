#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{   }
\include "english.ly"
%{
Filename: test2

     S
|(Sr  n)  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
\partial 4*2  \bar "|"  c'8( df'8 \acciaccatura {c'32}bf'4) \break

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