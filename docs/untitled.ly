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
StaffNotationUrl: http://ragapedia.local/compositions/untitled.jpg
ForceNotesUsed: false

     NRSNS
     .  .     +
| --S- ---- | N
              .  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
\partial 4*2  | r8 \afterGrace c'8~( { b32[ d'32 c'32 b32 c'32)] } c'4 | \partial 4*1  b4 \break

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
