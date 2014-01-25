#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = ""
composer = ""
  tagline = ""  % removed 
}
%{
 TimeSignature:5/4

S - - - - R - - - - 
 %}
  
melody = {
\time 5/4
\clef treble
\key c \major
\autoBeamOn  
\cadenzaOn
c'4~[ c'4 c'4~ c'4~ c'4~] d'4~[ d'4 d'4~ d'4~ d'4~]  \break        

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
