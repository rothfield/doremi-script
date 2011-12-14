#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{   }
\include "english.ly"
%{
          . .... ...: :::: :::
| Srgm PdnS Srgm PdnS Srgm Pdn-

   :::  :::: :... ....         
 | ndP- mgrS SndP mgrS SndP mgrS 

 | SndP mgrS SndP mgrS
    ... .... .::: ::::

  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
\bar "|"  c'16 df'16 ef'16 f'16 g'16 af'16 bf'16 c''16 c''16 df''16 ef''16 f''16 g''16 af''16 bf''16 c'''16 c'''16 df'''16 ef'''16 f'''16 g'''16 af'''16 bf'''8 \break
 \partial 4*0  \bar "|"  bf'''16 af'''16 g'''8 f'''16 ef'''16 df'''16 c'''16 c'''16 bf''16 af''16 g''16 f''16 ef''16 df''16 c''16 c'16 bf'16 af'16 g'16 f'16 ef'16 df'16 c'16 \break
 \partial 4*0  \bar "|"  c'16 bf16 af16 g16 f16 ef16 df16 c16 c16 bf,16 af,16 g,16 f,16 ef,16 df,16 c,16 \break

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
\layout { }
\midi { }
}