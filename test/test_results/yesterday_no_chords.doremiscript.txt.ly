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

Yes-ter-day  all my trou-bles seemed so far a-way
now it looks as though they're here to stay oh I be-lieve in Yes-ter-day
sudd-en-ly  I'm  not half the  man I used  to be
theres a sha-dow hang-ing o-ver me Oh I be-lieve in Yes-ter-day
why she had to go I don't know she would-n't say
I said some-thing wrong now I long for yes-ter-day day 




                               * 
1) |  RS S -- --  | -- GM P#D NS | N DD -- -- |

| -- DD Pm GR | m GG -- R- | S G R D | S GG - - |
                                   * 

                                 *  
2) |  RS S- -- --  | -- GM P#D  NS | N DD -- -- |

   | -- DD Pm GR | m GG -- R- | S G R D | S GG - - :|
                                      *


                      *         
3) || G -  G - |  D N S-  ND | N- -D P   DG | - - - - |

                                        1_____      2______
                   *                    *           *           
   | G - G - | D N S- ND | N- -D P N | (S P m G) :| S - - -     ||

 

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn
\cadenzaOn


 \bar "|"  d'8 c'8 c'4 r4 r4 \bar "|"  r4 e'8 fs'8 gs'8 a'8 b'8 c''8 \bar "|"  b'4 a'8 a'8 r4 r4 \bar "|" 
 \bar "|"  r4 a'8 a'8 g'8 f'8 e'8 d'8 \bar "|"  f'4 e'8 e'8 r4 d'4 \bar "|"  c'4 e'4 d'4 a4 \bar "|"  c'4 e'8 e'8 r4 r4 \bar "|" 
 \bar "|"  d'8 c'8 c'4 r4 r4 \bar "|"  r4 e'8 fs'8 gs'8 a'8 b'8 c''8 \bar "|"  b'4 a'8 a'8 r4 r4 \bar "|" 
 \bar "|"  r4 a'8 a'8 g'8 f'8 e'8 d'8 \bar "|"  f'4 e'8 e'8 r4 d'4 \bar "|"  c'4 e'4 d'4 a4 \bar "|"  c'4 e'8 e'8 r4 r4 \bar ":|" 
 \bar "||"   e'4 r4 e'4 r4 \bar "|"  a'4 b'4 c''4 b'8 a'8 \bar "|"  b'4 r8 a'8 g'4 a'8 e'8 \bar "|"  r4 r4 r4 r4 \bar "|" 
 \bar "|"  e'4 r4 e'4 r4 \bar "|"  a'4 b'4 c''4 b'8 a'8 \bar "|"  b'4 r8 a'8 g'4 b'4 \bar "|"  c''4( g'4 f'4 e'4) \bar ":|"  c''4 r4 r4 r4 \bar "||"  

}


text = \lyricmode {

}

\score{
\transpose c' f'
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