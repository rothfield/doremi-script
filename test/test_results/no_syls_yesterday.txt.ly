#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = "Yesterday"
composer = "McCartney"
  tagline = ""  % removed 
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

  Bb    C7      F       C/E  Dmi G     Bb   F 
| -- DD Pm GR | m GG -- R- | S G R D | S GG - - |
                                   * 

      F              Em7   A7    *   Dmi  Dmi/C 
2) |  RS S- -- --  | -- GM P#D  NS | N DD -- -- |

     Bb    C7      F       C/E  Dmi G     Bb   F 
   | -- DD Pm GR | m GG -- R- | S G R D | S GG - - :|
                                      *


      A11  A7     Dmi
                    C Bb  Dmi/A      C        F
                      *        G 
3) || G -  G - |  D N S-  ND | N- -D P   DG | - - - - |

     A11 A7    Dmi Bb Dmi/A      C      F           F
                 C         Gm           1_____      2______
                   *                    *           *           
   | G - G - | D N S- ND | N- -D P N | (S P m G) :| S - - -     ||

  
 %}
  
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn  
\cadenzaOn
\bar "|" d'8^"F"[ c'8] c'2.~ \bar "|" c'4 e'8[ fs'8] gs'8^"A7"[ a'8] b'8[ c''8] \bar "|" b'4 \acciaccatura { a'32[ f'32] }a'8^"i"[ a'8~ a'2] \bar "|"  \break        
 \bar "|" r4 a'8[ a'8] g'8^"C7"[ f'8] e'8[ d'8] \bar "|" f'4^"F" e'8[ e'8~ e'4] d'4^"C/E" \bar "|" c'4 \acciaccatura { a'32[ f'32] }e'4^"i" d'4 a4 \bar "|" c'4^"Bb" e'8[ e'8~ e'2] \bar "|"  \break        
 \bar "|" d'8^"F"[ c'8] c'2.~ \bar "|" c'4 e'8[ fs'8] gs'8^"A7"[ a'8] b'8[ c''8] \bar "|" b'4 \acciaccatura { a'32[ f'32] }a'8^"i"[ a'8~ a'2] \bar "|"  \break        
 \bar "|" r4 a'8[ a'8] g'8^"C7"[ f'8] e'8[ d'8] \bar "|" f'4^"F" e'8[ e'8~ e'4] d'4^"C/E" \bar "|" c'4 \acciaccatura { a'32[ f'32] }e'4^"i" d'4 a4 \bar "|" c'4^"Bb" e'8[ e'8~ e'2] \bar ":|"  \break        
 \bar "||"  e'2^"A11" e'2^"A7" \bar "|" a'4 \acciaccatura { a'32[ f'32] }b'4^"C" c''4^"Bb" b'8[ a'8] \bar "|" b'4~[ b'8] a'8 g'4^"C" a'8[ e'8]~ \bar "|" e'1 \bar "|"  \break        
 \bar "|" e'2^"A11" e'2^"A7" \bar "|" a'4 \acciaccatura { a'32[ f'32] }b'4^"C" c''4^"Bb" b'8[ a'8] \bar "|" b'4~[ b'8] a'8 g'4^"C" b'4 \bar "|" c''4(^"1_____"^"F" g'4 f'4 e'4) \bar ":|" c''1^"2______"^"F" \bar "||"   \break        

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
