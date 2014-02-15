{:key "B", :mode "Lydian", :kind :abc-composition}
#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 

%{
Key: B
Mode:Lydian

C :|

D E F    
%}
}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key b \lydian
\cadenzaOn
  c'4 \bar ":|"  
  d'4 e'4 f'4  
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
Usage:
 java -jar doremi.jar file1 file2 ... 
or use java -jar - to read from standard input
