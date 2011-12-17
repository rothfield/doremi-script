#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{   }
\include "english.ly"
%{
ForceSargamChars: SrGmMdN
Raga: Lalit
Filename: lalit1

| N r G mM | mmmm
  .  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
\bar "|"  b4 df'4 e'4 f'8 fs'8 \bar "|"  \partial 4*1  f'16 f'16 f'16 f'16 \break

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