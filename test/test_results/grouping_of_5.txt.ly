#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{


}
%{
                                       .
S----R-- --G----m ----P--- -D----N- ---S----

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn
\cadenzaOn


 c'8~c'32 d'16. r16 e'8~e'32 f'32 r8 g'8 r32 a'8~a'32 b'16 r16. c''8~c''32 \break

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