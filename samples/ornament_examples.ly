#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{   }
\include "english.ly"
%{
Filename: ornament_examples

   NRSNS   
   .  .    P   m  <G>    C F Dm G7+     <GPmGm>
| S         m   G   m- | S G m  P    |  m
                    
            
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
\bar "|"  \afterGrace c'4( { b32[ d'32 c'32 b32 c'32]) } \acciaccatura {g'32}f'4 \acciaccatura {f'32}e'4 \acciaccatura {e'32}f'4 \bar "|"  c'4^"C" e'4^"F" f'4^"Dm" g'4^"G7+" \bar "|"  \partial 4*1  \afterGrace f'4( { e'32[ g'32 f'32 e'32 f'32]) } \break

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