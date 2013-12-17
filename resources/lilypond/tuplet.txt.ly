 #(ly:set-option 'midi-extension "mid")
 \version "2.12.3"
 \include "english.ly"
 \header{ 
 
 
 tagline = ""  % removed 
 }
%{
| S-R ---
  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
\partial 4*2  \bar "|"  \times 2/5 {d'16 e'8 d'16 e'16 } d'4 \break

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
