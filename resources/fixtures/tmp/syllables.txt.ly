 #(ly:set-option 'midi-extension "mid")
 \version "2.12.3"
 \include "english.ly"
 \header{ 
 
 
 tagline = ""  % removed 
 }
%{
S -           R
good job
  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
c'4~ c'4 d'4 \break

}

text = \lyricmode {
 good job
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