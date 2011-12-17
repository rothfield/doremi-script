#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ title = "Happy Birthday" composer = "Traditional" }
\include "english.ly"
%{
Title: Happy Birthday
Filename: happy_birthday
Key: c
Author: Traditional
TimeSignature: 3/4

|: P-P | D P S | N - PP | D P R | S - PP | 
   . .   . .     .   ..   . .         ..  
ha-ppy birth-day to you ha-ppy birth-day to you ha-ppy

                                     1.____   2.______
| P G S | (N - - | D) - mm | G S R | S -   :| S -P DP | n - -
           .       .                             . ..   .
  birth-day dear Jim ha-ppy birth-day to you  you and ma-ny more

  
%}
melody = {
\clef treble
\key c \major
\time 3/4
\autoBeamOn  
\partial 4*1  \bar "|:"  \times 2/3 {  g4 g8 \bar "|"  \partial 4*3  } a4 g4 c'4 \bar "|"  \partial 4*3  b4~ b4 g8 g8 \bar "|"  \partial 4*3  a4 g4 d'4 \bar "|"  \partial 4*3  c'4~ c'4 g8 g8 \bar "|"  \partial 4*0  \break
 \partial 4*3  \bar "|"  g'4 e'4 c'4 \bar "|"  \partial 4*3  b4~( b4~ b4 \bar "|"  \partial 4*3  a4~) a4 f'8 f'8 \bar "|"  \partial 4*3  e'4 c'4 d'4 \bar "|"  \partial 4*2  c'4~^"1.____" c'4 \bar ":|"  \partial 4*3  c'4~^"2.______" c'8 g8 a8 g8 \bar "|"  \partial 4*3  bf4~ bf4~ bf4 \break

}

text = \lyricmode {
ha- ppy birth- day to you ha- ppy birth- day to you ha- ppy birth- day dear Jim ha- ppy birth- day to you you and ma- ny more
}

\score{
\transpose c' c'
<<
\new Voice = "one" {
  \melody
}
\new Lyrics \lyricsto "one" \text
>>
\layout { }
\midi { }
}