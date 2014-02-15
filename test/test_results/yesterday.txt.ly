#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{
title = "Yesterday"
composer = "McCartney"
}
%{

%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\cadenzaOn


  d'8[^"F" c'8] c'4 r4 r4 \bar "|"  r4^"Em7" e'8[ fs'8] gs'8[^"A7" a'8] b'8[ c''8] \bar "|"  b'4 \grace {a'16[[ f'16]}a'8^"i" a'8] r4 r4 \bar "|"  
  r4^"Bb" a'8[ a'8] g'8[^"C7" f'8] e'8[ d'8] \bar "|"  f'4^"F" e'8[ e'8] r4 d'4^"C/E" \bar "|"  c'4 \grace {a'16[ f'16]}e'4^"i" d'4 a4 \bar "|"  c'4^"Bb" e'8[ e'8] r4^"F" r4 \bar "||"   
  d'8[^"F" c'8] c'4 r4 r4 \bar "|"  r4^"Em7" e'8[ fs'8] gs'8[^"A7" a'8] b'8[ c''8] \bar "|"  b'4 \grace {a'16[[ f'16]}a'8^"i" a'8] r4 r4 \bar "|"  
  r4^"Bb" a'8[ a'8] g'8[^"C7" f'8] e'8[ d'8] \bar "|"  f'4^"F" e'8[ e'8] r4 d'4^"C/E" \bar "|"  c'4 \grace {a'16[ f'16]}e'4^"i" d'4 a4 \bar "|"  c'4^"Bb" e'8[ e'8] r4^"F" r4 \bar ":|"  
 \bar "||"   e'4^"A11" r4 \grace {d'16}e'4^"A7" r4 \bar "|"  a'4 \grace {a'16[ f'16]}b'4^"i" c''4^"Bb" b'8[ a'8] \bar "|"  b'4 r8 a'8 g'4^"C" a'8[ e'8] \bar "|"  r4^"F" r4 r4 r4 \bar "|"  
  e'4^"A11" r4 e'4^"A7" r4 \bar "|"  a'4 \grace {a'16[ f'16]}b'4^"i" c''4^"Bb" b'8[ a'8] \bar "|"  b'4 r8 a'8 g'4^"C" b'4 \bar "|"  c''4(^"F"^"1_____" g'4 f'4 e'4) \bar ":|"  c''4^"F"^"2______" r4 r4 r4 \bar "||"   
 
}


text = \lyricmode {
yes- ter day all my trou- bles seemed so far a- way now it looks as though they're here to stay oh I be- lieve in Yes- ter- day sudd- en- ly I'm not half the man I used to be theres a sha- dow hang- ing o- ver me Oh I be- lieve in Yes- ter- day why she had to go I don't know she would- n't say I said some- thing wrong now I long for yes- ter- day    day 
}

\score{
\transpose c' f'
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