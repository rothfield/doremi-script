
\version "2.12.3"
% automatically converted from bansuri.xml

\header {
    encodingsoftware = DoremiScript
    tagline = DoremiScript
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
\layout {
    \context { \Score
        autoBeaming = ##f
        }
    }
PartPOneVoiceOne =  \relative c' {
    \clef "treble" \key c \phrygian \numericTimeSignature\time 4/4
    \partial 4 \repeat volta 2 {
        c8 ^"phrygian " ( des8 | % 2
        \grace { c32 } bes4 ) c4 es8 ( f8 g8 \grace { bes32 } as8 ) \bar
        ".|."
        | % 3
        g4 ~ g4 g4 g4 | % 4
        g4 ~ g4 a4 bes4 | % 5
        g4 as4 g4 as4 | % 6
        g8 ( f8 g16 f16 bes16 g16 ) \grace { f32 es32 f32 } es4 ( f4 )
        \bar ".|."
        | % 7
        g16 ( as16 bes'16 c,,16 ) ~ c4 es4 c4 | % 8
        as'8 ( g16 f16 es4 ) g4 f4 | % 9
        des4 ~ des4 c4 }
    }

PartPOneVoiceOneLyricsOne =  \lyricmode { "ban-" "su-" ri "ba-" ja "ra-"
    hi "dhu-" na "ma-" "dhu-" ra "kan-" "nai-" ya "khe-" "la-" ta "ga-"
    "wa-" ta "ho-" ri }

% The score definition
\new Staff <<
    \context Staff << 
        \context Voice = "PartPOneVoiceOne" { \PartPOneVoiceOne }
        \new Lyrics \lyricsto "PartPOneVoiceOne" \PartPOneVoiceOneLyricsOne
        >>
    >>

