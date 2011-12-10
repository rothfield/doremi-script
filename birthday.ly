
\version "2.12.3"
% automatically converted from birthday.xml

\header {
    encodingsoftware = "Finale 2009 for Windows"
    tagline = "Finale 2009 for Windows"
    composer = "Mildred J. Hill,"
    copyright = "All Rights Reserved"
    encodingdate = "2010-01-29"
    }

#(set-global-staff-size 24.0900948425)
\paper {
    paper-width = 21.59\cm
    paper-height = 27.94\cm
    top-margin = 1.27\cm
    botton-margin = 1.27\cm
    left-margin = 2.54\cm
    right-margin = 1.27\cm
    between-system-space = 2.67\cm
    page-top-space = 1.74\cm
    }
\layout {
    \context { \Score
        autoBeaming = ##f
        }
    }
PartPOneVoiceOne =  \relative c' {
    \clef "treble" \key f \major \time 3/4 \partial 4 c8. [ c16 ] | % 1
    | % 1
    d4 c4 f4 | % 2
    | % 2
    e2 c8 [ c8 ] | % 3
    d4 c4 g'4 | % 4
    | % 4
    f2 c8. [ c16 ] | % 5
    c'4 a4 f4 | % 6
    | % 6
    e4 d4 ^\fermata bes'8. [ bes16 ] | % 7
    | % 7
    a4 f4 g4 | % 8
    | % 8
    f2 r4 \bar "|."
    }

PartPOneVoiceOneChords =  \chordmode {
    | % 1
    | % 1
    s4 f4:5 | % 2
    | % 2
    s2 c2:7 | % 3
    | % 4
    | % 4
    s1 f2:5 | % 5
    | % 6
    | % 6
    s1 bes4:5 | % 7
    | % 7
    s2 c4:7 | % 8
    | % 8
    s2 f2:5 }


% The score definition
\new Staff <<
    \context ChordNames = "PartPOneVoiceOneChords" \PartPOneVoiceOneChords
    \context Staff <<
        \context Voice = "PartPOneVoiceOne" { \PartPOneVoiceOne }
        >>
    >>

