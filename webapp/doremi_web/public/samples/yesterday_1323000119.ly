#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ title = "Yesterday" composer = "McCartney" }
\include "english.ly"
%{
Filename: yesterday
Key: f
Title: Yesterday
Author: McCartney
                                                       *
|: <R   S>   S -- --  | -- <G   M> <P#   D>  <N        S> | <N   D> D -- -- |
    yes-ter  day            all my  trou-bles seemed   so    far a- way

| -- <D   D> <P     m> <G      R> |   m   <G  G>  -- R- | S G  R     D | S  <G   G> - - :|
                                                                     .
      now it  looks as  though theyre here to stay   oh   I be-lieve in  Yes-ter-day
  
%}
melody = {
\clef treble
\key c \major
\time 4/4
\autoBeamOn  
\bar "|:"  d'8 c'8 c'4~ c'4~ c'4~ \bar "|"  c'4 e'8 fs'8 gs'8 a'8 b'8 c''8 \bar "|"  b'8 a'8 a'4~ a'4~ a'4 \bar "|"  \break
 \bar "|"  r4 a'8 a'8 g'8 f'8 e'8 d'8 \bar "|"  f'4 e'8 e'8~ e'4 d'4 \bar "|"  c'4 e'4 d'4 a4 \bar "|"  c'4 e'8 e'8~ e'4~ e'4 \bar ":|"  \break

}

text = \lyricmode {
yes- ter day all my trou- bles seemed so far a- way now it looks as though theyre here to stay oh I be- lieve in Yes- ter- day
}

\score{
\transpose c' f'
<<
\new Voice = "one" {
  \melody
}
\new Lyrics \lyricsto "one" \text
>>
\layout { }
\midi { }
}