 #(ly:set-option 'midi-extension "mid")
 \version "2.12.3"
 \include "english.ly"
 \header{ 
 title = "untitled"
 
 tagline = ""  % removed 
 }
%{
Title: untitled
Filename: untitled
Key: C
Mode: major
TimeSignature: 4/4
ApplyHyphenatedLyrics: true
ForceNotesUsed: false

   R
| G  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
\partial 4*1  | \afterGrace e'4( { d'32) } \break

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
