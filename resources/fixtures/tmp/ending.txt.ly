 #(ly:set-option 'midi-extension "mid")
 \version "2.12.3"
 \include "english.ly"
 \header{ 
 
 
 tagline = ""  % removed 
 }
%{
  1.____       2.____
| S - - -   || R - - - 
  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
| c'4~^"1.____" c'4~ c'4~ c'4 \bar "||"  d'4~^"2.____" d'4~ d'4~ d'4 \break

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