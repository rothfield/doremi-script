#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ title = "Yesterday" composer = "McCartney" }
\include "english.ly"
%{
Filename: yesterday
Key: F
Mode: major
Title: Yesterday
Author: McCartney
Time: 4/4

      F             Em7   A7   *   Dmi  Dmi/C 
1) |  RS S -- --  | -- GM P#D NS | N DD -- -- |
      yes-ter day      all my trou-bles seemed so far a- way

  Bb    C7      F       C/E  Dmi G     Bb   F 
| -- DD Pm GR | m GG -- R- | S G R D | S GG - - |
                                   * 
now it looks as though they're here to stay oh I be-lieve in  Yes-ter-day

      F              Em7   A7    *   Dmi  Dmi/C 
2) |  RS S- -- --  | -- GM P#D  NS | N DD -- -- |
      sudd- en- ly   I'm  not half the  man I  used  to be

     Bb    C7      F       C/E  Dmi G     Bb   F 
   | -- DD Pm GR | m GG -- R- | S G R D | S GG - - :|
                                      *
        theres a sha-  dow hang-ing o-ver  me Oh I be-lieve in  Yes-ter-day


      A11  A7     Dmi
                    C Bb  Dmi/A      C        F
                      *        G 
3) || G -  G - |  D N S-  ND | N- -D P   DG | - - - - |
      why she     had  to  go  I  don't   know  she would- n't   say

     A11 A7    Dmi Bb Dmi/A      C      F           F
                 C         Gm           1_____      2______
                   *                    *           *           
   | G - G - | D N S- ND | N- -D P N | (S P m G) :| S - - -     ||
     I said some-thing wrong now I long for  yes-ter-day day 

 
  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
\partial 4*0  \bar "|"  d'8^"F" c'8 c'4~ c'4~ c'4~ \bar "|"  c'4^"Em7" e'8 fs'8 gs'8^"A7" a'8 b'8 c''8 \bar "|"  b'4^"Dmi" a'8 a'8~ a'4~^"Dmi/C" a'4 \bar "|"  \break
 \bar "|"  r4^"Bb" a'8 a'8 g'8^"C7" f'8 e'8 d'8 \bar "|"  f'4^"F" e'8 e'8~ e'4 d'4^"C/E" \bar "|"  c'4^"Dmi" e'4 d'4^"G" a4 \bar "|"  c'4^"Bb" e'8 e'8~ e'4~^"F" e'4 \bar "|"  \break
 \partial 4*0  \bar "|"  d'8^"F" c'8 c'4~ c'4~ c'4~ \bar "|"  c'4^"Em7" e'8 fs'8 gs'8^"A7" a'8 b'8 c''8 \bar "|"  b'4^"Dmi" a'8 a'8~ a'4~^"Dmi/C" a'4 \bar "|"  \break
 \partial 4*0  \bar "|"  r4^"Bb" a'8 a'8 g'8^"C7" f'8 e'8 d'8 \bar "|"  f'4^"F" e'8 e'8~ e'4 d'4^"C/E" \bar "|"  c'4^"Dmi" e'4 d'4^"G" a4 \bar "|"  c'4^"Bb" e'8 e'8~ e'4~^"F" e'4 \bar ":|"  \break
 \partial 4*0  \bar "||"  e'4~^"A11" e'4 e'4~^"A7" e'4 \bar "|"  a'4^"Dmi" b'4^"C" c''4^"Bb" b'8^"Dmi/A" a'8 \bar "|"  b'4~^"G" b'8 a'8 g'4^"C" a'8 e'8~ \bar "|"  e'4~^"F" e'4~ e'4~ e'4 \bar "|"  \break
 \partial 4*0  \bar "|"  e'4~^"A11" e'4 e'4~^"A7" e'4 \bar "|"  a'4^"Dmi" b'4^"C" c''4^"Bb" b'8^"Dmi/A" a'8 \bar "|"  b'4~^"Gm" b'8 a'8 g'4^"C" b'4 \bar "|"  c''4(^"1_____"^"F" g'4 f'4 e'4) \bar ":|"  c''4~^"2______"^"F" c''4~ c''4~ c''4 \bar "||"  \break

}

text = \lyricmode {
yes- ter day all my trou- bles seemed so far a- way now it looks as though they're here to stay oh I be- lieve in Yes- ter- day sudd- en- ly I'm not half the man I used to be theres a sha- dow hang- ing o- ver me Oh I be- lieve in Yes- ter- day why she had to go I don't know she would- n't say I said some- thing wrong now I long for yes- ter- day day
}

\score{
\transpose c' f'
<<
\new Voice = "one" {
  \melody
}
\new Lyrics \lyricsto "one" \text
>>
\layout { }
\midi { }
}