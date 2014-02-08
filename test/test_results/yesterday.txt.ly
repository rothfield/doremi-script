#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = "Yesterday"
composer = ""
}
%{
Filename: yesterday
Key: F
Mode: major
Title: Yesterday
Author: McCartney
Time: 4/4
EnteredBy: John Rothfield

      F             Em7   A7   *   Dmi  Dmi/C 
1) |  RS S -- --  | -- GM P#D NS | N DD -- -- |
      yes-ter day      all my trou-bles seemed so far a- way

  Bb    C7      F       C/E  Dmi G     Bb   F 
| -- DD Pm GR | m GG -- R- | S G R D | S GG - - ||
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
          R           *        G 
3) || G -  G - |  D N S-  ND | N- -D P   DG | - - - - |
      why she     had  to  go  I  don't   know  she would- n't   say

     A11 A7    Dmi Bb Dmi/A      C      F           F
                 C         Gm           1_____      2______
                   *                    *           *           
   | G - G - | D N S- ND | N- -D P N | (S P m G) :| S - - -     ||
     I said some-thing wrong now I long for  yes-ter-day day 

 


%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn
\cadenzaOn


 \bar "|"    d'8 c'8  c'4 r4 r4 \bar "|"  r4   e'8 '8   gs'8 a'8   b'8 c'8 \bar "|"   b'4   a'8 a'8 r4 r4 \bar "|"  \bar "|"  r4   a'8 a'8   g'8 f'8   e'8 d'8 \bar "|"   f'4   e'8 e'8 r4  d'4 \bar "|"   c'4  e'4  d'4  a'4 \bar "|"   c'4   e'8 e'8 r4 r4 \bar "||"   \bar "|"    d'8 c'8  c'4 r4 r4 \bar "|"  r4   e'8 '8   gs'8 a'8   b'8 c'8 \bar "|"   b'4   a'8 a'8 r4 r4 \bar "|"  \bar "|"  r4   a'8 a'8   g'8 f'8   e'8 d'8 \bar "|"   f'4   e'8 e'8 r4  d'4 \bar "|"   c'4  e'4  d'4  a'4 \bar "|"   c'4   e'8 e'8 r4 r4 \bar ":|"  \bar "||"    e'4 r4  e'4 r4 \bar "|"   a'4  b'4  c'4   b'8 a'8 \bar "|"   b'4  r8 a'8  g'4   a'8 e'8 \bar "|"  r4 r4 r4 r4 \bar "|"  \bar "|"   e'4 r4  e'4 r4 \bar "|"   a'4  b'4  c'4   b'8 a'8 \bar "|"   b'4  r8 a'8  g'4  b'4 \bar "|"   c'4  g'4  f'4  e'4 \bar ":|"   c'4 r4 r4 r4 \bar "||"  
}


text = \lyricmode {
yes- ter day all my trou- bles seemed so far a- way now it looks as though they're here to stay oh I in be- lieve Yes- ter- day sudd- en- ly I'm not half the man I used to be theres a sha- dow hang- ing o- ver me Oh I in be- lieve Yes- ter- day why had she to don't go I know she would- n't say I said some- now thing wrong I long for yes- ter- day day
}

\score{
\transpose c' d'
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
\midi {
\context {
\Score
tempoWholesPerMinute = #(ly:make-moment 200 4)
}
}
}