#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = "Bansuri"
composer = ""
}
%{
Rag:Bhairavi
Tal:Tintal
Title:Bansuri
Author:Traditional
Source:AAK
Mode: phrygian
Filename: bansuriv3
Time: 4/4
Key: D


          
                               i            IV         . 
         3S             n      +            2         DSnDn
1)|: (Sr | n) S   (gm <P d)> | P - P  P   | P - D    n     |
           .
      ban-    su-  ri          ba- ja ra-   hi  dhu- na

  0  ~                 3           mgm        +  .     *  *   
| P  d   P    d    |  (Pm   PmnP)    (g m) | (PdnS) -- g  S |
  ma-dhu-ra   kan-     nai-           ya      khe-     la-ta

   2               0     
                Pm ~
| (d-Pm  g) P  m | r - S :|
   ga-      wa-ta  ho- ri

               .
              <g>
            .   . .
| g m d n | S - r S |
ji-na ja-u san-ga

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \phrygian
\autoBeamOn
\cadenzaOn


 \bar "|:"    c'8 df'8 \bar "|"   bf'4  c'4   ef'8 f'8   g'8 af'8 \bar "|"   g'4 r4  g'4  g'4 \bar "|"   g'4 r4  a'4  bf'4 \bar "|"  \bar "|"   g'4  af'4  g'4  af'4 \bar "|"    g'8 f'8     g'16 f'16 bf'16 g'16  ef'4  f'4 \bar "|"      g'16 af'16 bf'16 c'16 r4  ef'4  c'4 \bar "|"  \bar "|"     af'8 g'16 f'16  ef'4  g'4  f'4 \bar "|"   df'4 r4  c'4 \bar ":|"  \bar "|"   ef'4  f'4  af'4  bf'4 \bar "|"   c'4 r4  df'4  c'4 \bar "|" 
}


text = \lyricmode {
ban- su- ri ba- ja ra- hi dhu- na ma- dhu- ra kan- nai- ta ya khe- la- ga- wa- ri ta ho- ji- na ja- u san- ga
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