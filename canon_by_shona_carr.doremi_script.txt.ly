#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = "Canon"
composer = "Shona Carr"
  tagline = ""  % removed 
}
%{
 Title: Canon
Key: A 
Mode: dorian
Author: Shona Carr
TimeSignature: 4/4

|: S S SR (Rg) | RS nS RS n P |
                    .     . .

| S S SR (Rg) | RS nR S S :|
                   .

         .    DP                            . 
|: PD nP SD <n   P> | G (GP) (Gm) m | PD nP SD nP | g (gR) S SS :|

          .  ..   . ..  ..  . 
|: P PP (nS) SS | S SS (Rg) g |

         .  ..   . ..  ..  . 
| P PP (nS) SS | S SS (RG) G- :|
 
 %}
  
melody = {
\time 4/4
\clef treble
\key c \dorian
\autoBeamOn  
\cadenzaOn
\bar "|:"  c'4 c'4 c'8[ d'8] d'8[( ef'8]) \bar "|"  d'8[ c'8] bf8[ c'8] d'8[ c'8] bf4 g4 \bar "|" \break

\bar "|"  c'4 c'4 c'8[ d'8] d'8[( ef'8]) \bar "|"  d'8[ c'8] bf8[ d'8] c'4 c'4 \bar ":|" \break

\bar "|:"  g'8[ a'8] bf'8[ g'8] c''8[ a'8] bf'8[ g'8] \bar "|"  e'4 e'8[( g'8]) e'8[( f'8]) f'4 \bar "|"  g'8[ a'8] bf'8[ g'8] c''8[ a'8] bf'8[ g'8] \bar "|"  ef'4 ef'8[( d'8]) c'4 c'8[ c'8] \bar ":|" \break

\bar "|:"  g'4 g'8[ g'8] bf'8[( c''8]) c''8[ c''8] \bar "|"  c''4 c''8[ c''8] d''8[( ef''8]) ef''4 \bar "|" \break

\bar "|"  g'4 g'8[ g'8] bf'8[( c''8]) c''8[ c''8] \bar "|"  c''4 c''8[ c''8] d''8[( e''8]) e''4  \bar ":|" \break

}

text = \lyricmode {
  
}

\score{
\transpose c' a'
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
