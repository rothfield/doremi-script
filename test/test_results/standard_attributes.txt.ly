#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = "Sargam"
composer = ""
}
%{
Rag:Bhairavi
Tal:Dadra
Title:Sargam
Author:Traditional
Source:AAK
Mode: phrygian
Filename: bhairavi sargam 
TimeSignature: 3/4
Key: D

   0             +       0    RmgRg    g+
|: -- Rg -S R- | g - - | - - g       |  r S - | - :|

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \phrygian
\autoBeamOn
\cadenzaOn


 \bar "|:"  r4 d'8 ef'8 r8 c'8 d'4 \bar "|"  ef'4 r4 r4 \bar "|"  r4 r4 ef'4 \bar "|"  df'4 c'4 r4 \bar "|"  r4 \bar ":|" 

}


text = \lyricmode {

}

\score{
\transpose c' d'
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