 #(ly:set-option 'midi-extension "mid")
 \version "2.12.3"
 \include "english.ly"
 \header{ 
 
 
 tagline = ""  % removed 
 }
%{
                 .   ..
| (PG) | S G P | S - GR  |
Oh say can you see by the
  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
| g'8( e'8) | c'4 e'4 g'4 | c''4~ c''4 e''8 d''8 | \break

}

text = \lyricmode {
 Oh say can you see by the
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