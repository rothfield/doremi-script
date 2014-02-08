#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = "Hapy Birthday"
composer = ""
}
%{
Title: Hapy Birthday

1) |: SS | R S m | G - :| 
ha-ppy birth-day to you

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn
\cadenzaOn


 \bar "|:"    c'8 c'8 \bar "|"   d'4  c'4  f'4 \bar "|"   e'4 r4 \bar ":|" 
}


text = \lyricmode {
ha- ppy birth- day to you
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