#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
}
%{
Hi john

|SSSS rrrrr mmmmm PPPPPP SSS rrrrr gggg

| ddddddddddd 

| mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm

| PPPPP ddddd

|SSSS rrrrr mmmmm PPPPPP

| ddddddddddd

| mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm

| PPPPP ddddd



|SSSS rrrrr mmmmm PPPPPP

| ddddddddddd

| mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm

| PPPPP ddddd


|SSSS rrrrr mmmmm PPPPPP

| ddddddddddd

| mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm

| PPPPP ddddd


|SSSS rrrrr mmmmm PPPPPP

| ddddddddddd

| mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm

| PPPPP ddddd


|SSSS rrrrr mmmmm PPPPPP

| ddddddddddd

| mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm

| PPPPP ddddd


|SSSS rrrrr mmmmm PPPPPP

| ddddddddddd

| mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm

| PPPPP ddddd


%}
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c 
\major
\cadenzaOn
  c'16[ c'16 c'16 c'16] \times 4/5{ df'16[ df'16 df'16 df'16 df'16] }  \times 4/5{ f'16[ f'16 f'16 f'16 f'16] }  \times 4/6{ g'16[ g'16 g'16 g'16 g'16 g'16] }  \times 2/3{ c'8[ c'8 c'8] }  \times 4/5{ df'16[ df'16 df'16 df'16 df'16] }  ef'16[ ef'16 ef'16 ef'16] \bar "" \break 
  \times 8/11{ af'32[ af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32] }  \bar "" \break 
  \times 16/31{ f'64[ f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64] }  \bar "" \break 
  \times 4/5{ g'16[ g'16 g'16 g'16 g'16] }  \times 4/5{ af'16[ af'16 af'16 af'16 af'16] }  \bar "" \break 
  c'16[ c'16 c'16 c'16] \times 4/5{ df'16[ df'16 df'16 df'16 df'16] }  \times 4/5{ f'16[ f'16 f'16 f'16 f'16] }  \times 4/6{ g'16[ g'16 g'16 g'16 g'16 g'16] }  \bar "" \break 
  \times 8/11{ af'32[ af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32] }  \bar "" \break 
  \times 16/31{ f'64[ f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64] }  \bar "" \break 
  \times 4/5{ g'16[ g'16 g'16 g'16 g'16] }  \times 4/5{ af'16[ af'16 af'16 af'16 af'16] }  \bar "" \break 
  c'16[ c'16 c'16 c'16] \times 4/5{ df'16[ df'16 df'16 df'16 df'16] }  \times 4/5{ f'16[ f'16 f'16 f'16 f'16] }  \times 4/6{ g'16[ g'16 g'16 g'16 g'16 g'16] }  \bar "" \break 
  \times 8/11{ af'32[ af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32] }  \bar "" \break 
  \times 16/31{ f'64[ f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64] }  \bar "" \break 
  \times 4/5{ g'16[ g'16 g'16 g'16 g'16] }  \times 4/5{ af'16[ af'16 af'16 af'16 af'16] }  \bar "" \break 
  c'16[ c'16 c'16 c'16] \times 4/5{ df'16[ df'16 df'16 df'16 df'16] }  \times 4/5{ f'16[ f'16 f'16 f'16 f'16] }  \times 4/6{ g'16[ g'16 g'16 g'16 g'16 g'16] }  \bar "" \break 
  \times 8/11{ af'32[ af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32] }  \bar "" \break 
  \times 16/31{ f'64[ f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64] }  \bar "" \break 
  \times 4/5{ g'16[ g'16 g'16 g'16 g'16] }  \times 4/5{ af'16[ af'16 af'16 af'16 af'16] }  \bar "" \break 
  c'16[ c'16 c'16 c'16] \times 4/5{ df'16[ df'16 df'16 df'16 df'16] }  \times 4/5{ f'16[ f'16 f'16 f'16 f'16] }  \times 4/6{ g'16[ g'16 g'16 g'16 g'16 g'16] }  \bar "" \break 
  \times 8/11{ af'32[ af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32] }  \bar "" \break 
  \times 16/31{ f'64[ f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64] }  \bar "" \break 
  \times 4/5{ g'16[ g'16 g'16 g'16 g'16] }  \times 4/5{ af'16[ af'16 af'16 af'16 af'16] }  \bar "" \break 
  c'16[ c'16 c'16 c'16] \times 4/5{ df'16[ df'16 df'16 df'16 df'16] }  \times 4/5{ f'16[ f'16 f'16 f'16 f'16] }  \times 4/6{ g'16[ g'16 g'16 g'16 g'16 g'16] }  \bar "" \break 
  \times 8/11{ af'32[ af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32] }  \bar "" \break 
  \times 16/31{ f'64[ f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64] }  \bar "" \break 
  \times 4/5{ g'16[ g'16 g'16 g'16 g'16] }  \times 4/5{ af'16[ af'16 af'16 af'16 af'16] }  \bar "" \break 
  c'16[ c'16 c'16 c'16] \times 4/5{ df'16[ df'16 df'16 df'16 df'16] }  \times 4/5{ f'16[ f'16 f'16 f'16 f'16] }  \times 4/6{ g'16[ g'16 g'16 g'16 g'16 g'16] }  \bar "" \break 
  \times 8/11{ af'32[ af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32 af'32] }  \bar "" \break 
  \times 16/31{ f'64[ f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64 f'64] }  \bar "" \break 
  \times 4/5{ g'16[ g'16 g'16 g'16 g'16] }  \times 4/5{ af'16[ af'16 af'16 af'16 af'16] }  \bar "" \break 
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