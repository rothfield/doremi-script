#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{   }
\include "english.ly"
%{
ForceSargamChars: SrGmMdN
Raga: Lalit
Filename: lalit1_

| N r G mM | mmmm MmGr
  .  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
\bar "|"  b4 df'4 e'4 f'8 fs'8 \bar "|"  \partial 4*2  f'16 f'16 f'16 f'16 fs'16 f'16 e'16 df'16 \break

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