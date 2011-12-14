#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ title = "Khammaj"  }
\include "english.ly"
%{
Filename:khammaj_rupak
Title: Khammaj
Key: D 
Mode: mixolydian
TimeSignature: 7/4
           
                             .           .   .
| N S - G - m - | P D -  N - S - | N - - S - R - |
  .               

  
%}
melody = {
\clef treble
\key c \mixolydian
\time 7/4
\autoBeamOn  
\bar "|"  b4 c'4~ c'4 e'4~ e'4 f'4~ f'4 \bar "|"  g'4 a'4~ a'4 b'4~ b'4 c''4~ c''4 \bar "|"  b'4~ b'4~ b'4 c''4~ c''4 d''4~ d''4 \bar "|"  \break

}

text = \lyricmode {

}

\score{
\transpose c' d'
<<
\new Voice = "one" {
  \melody
}
\new Lyrics \lyricsto "one" \text
>>
\layout { }
\midi { }
}