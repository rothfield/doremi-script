#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
TimeSignature: 5/4

S R G m P D N 

%}
melody = {
\time 5/4
\clef treble
\key c 
\major
\cadenzaOn
  c'4 d'4 e'4 f'4 g'4 a'4 b'4 \break 
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
}
}
\midi {
\context {
\Score
tempoWholesPerMinute = #(ly:make-moment 200 4)
}
}
}