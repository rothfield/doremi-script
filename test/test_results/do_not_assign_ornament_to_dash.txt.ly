#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
                 .   
                 g 
              .   . . 
| g m (d n) | S - r S | 
  ji-na ja-     u san-ga  
                        

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  ef'4 f'4 af'4( bf'4) \bar "|"  c''4 r4 \grace {ef''16}df''4 c''4 \bar "|" \bar "" \break 
 }
text = \lyricmode {
ji- na ja-  u san- ga 
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