#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = ""
composer = ""
  tagline = ""  % removed 
}
%{
                                *
1) |  RS S -- --  | -- GM P#D NS | N DD -- -- |
      yes-ter day      all my trou-bles seemed so far a- way

| -- DD Pm GR | m GG -- R- | S G R D | S GG - - ||
                                   * 
now it looks as though they're here to stay oh I be-lieve in  Yes-ter-day

 
 
 %}
  
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn  
\cadenzaOn
\bar "|" d'8[ c'8] c'2.~ \bar "|" c'4 e'8[ fs'8] gs'8[ a'8] b'8[ c''8] \bar "|" b'4 a'8[ a'8~ a'2] \bar "|"  \break        
 \bar "|" r4 a'8[ a'8] g'8[ f'8] e'8[ d'8] \bar "|" f'4 e'8[ e'8~ e'4] d'4 \bar "|" c'4 e'4 d'4 a4 \bar "|" c'4 e'8[ e'8~ e'2] \bar "||"   \break        

}

text = \lyricmode {
  yes- ter day all my trou- bles seemed so far a- way now it looks as though they're here to stay oh I be- lieve in Yes- ter- day
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
