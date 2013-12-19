#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = "{{title}}"
composer = "{{author}}"
  tagline = ""  % removed 
}
{{src-snippet}}  
melody = {
\clef treble
{{key-snippet}}
{{time-signature-snippet}}
\autoBeamOn  
{{notes}}
}

text = \lyricmode {
  {{extracted-lyrics}}
}

\score{
{{transpose-snip}}
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
	{{omit-time-signature-snippet}}
  }
\midi { 
  \context {
    \Score
    tempoWholesPerMinute = #(ly:make-moment {{beats-per-minute}} 4)
   }
 }
}
