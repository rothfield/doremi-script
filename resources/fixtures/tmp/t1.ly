 #(ly:set-option 'midi-extension "mid")
 \version "2.12.3"
 \include "english.ly"
 \header{ 
 
 composer = "John"
 tagline = ""  % removed 
 }
%{
Author: John
Key: D 
Mode: major

  Fm7
| S
  
%}
melody = {
\clef treble
\key d \major
\time 4/4
\autoBeamOn  
| c'4^"Fm7" \break

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