
\version "2.12.3"
% automatically converted from tmpmscore.xml

\header {
    encodingsoftware = "MuseScore 0.9.6"
    tagline = "MuseScore 0.9.6"
    encodingdate = "2011-12-10"
    title = Bansuri
    }

#(set-global-staff-size 20.0762645669)
\paper {
    paper-width = 21.0\cm
    paper-height = 29.7\cm
    top-margin = 1.0\cm
    botton-margin = 2.0\cm
    left-margin = 1.0\cm
    right-margin = 1.0\cm
    }
PartPOneVoiceOne =  \relative c' {
    \clef "treble" \key c \major \time 4/4 c8 des8 s2. | % 2
    bes4 c4 es8 ( f8 g8 as8 | % 3
    g4 ~ g4 g4 g4 | % 4
    g4 ~ g4 a4 bes4 | % 5
    g4 as4 g4 as4 | % 6
    g8 f8 g16 f16 bes16 g16 ) es4 ( f4 ) | % 7
    g16 ( as16 bes16 c16 ) ~ c4 es4 c4 | % 8
    as8 ( g16 f16 es4 g4 f4 | % 9
    des4 ~ des4 c4 }

PartPOneVoiceOneLyricsOne =  \lyricmode { "ban-" \skip4 \skip4 "su-" ri
    ya "khe-" "la-" ta "ga-" }

% The score definition
\new Staff <<
    \context Staff << 
        \context Voice = "PartPOneVoiceOne" { \PartPOneVoiceOne }
        \new Lyrics \lyricsto "PartPOneVoiceOne" \PartPOneVoiceOneLyricsOne
        >>
    >>

