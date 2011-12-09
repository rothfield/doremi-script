
\version "2.12.3"
% automatically converted from musicXML.xml

\header {
    copyright = "All Rights Reserved"
    encodingdate = "2009-09-04"
    tagline = "MuseScore 0.9.5"
    encodingsoftware = "MuseScore 0.9.5"
    composer = "John Lennon, Paul McCartney"
    poet = "John Lennon, Paul McCartney"
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
PartPOneVoiceOne =  \relative g' {
    \clef "treble" \key f \major \time 4/4 | % 1
    | % 1
    g8 -"Moderato" [ f8 ] f2. | % 2
    | % 2
    r4 a8 [ b8 ] cis8 [ d8 e8 f8 ] | % 3
    | % 3
    e8. [ d16 ] d2. | % 4
    | % 4
    r4 d8 [ d8 ] c8 [ bes8 a8 g8 ] | % 5
    | % 5
    bes4 a8 [ a8 ~ ] a4 g4 | % 6
    | % 6
    f4 a8 [ g8 ~ ] g4. d8 | % 7
    | % 7
    f4 a8 [ a8 ~ ] a2 \repeat volta 2 {
        | % 8
        | % 8
        a2 a2 | % 9
        | % 9
        d4 e4 f4 e8 [ d8 ] | \barNumberCheck #10
        | \barNumberCheck #10
        e4. d8 c4 d4 | % 11
        | % 11
        a1 | % 12
        | % 12
        a2 a2 | % 13
        | % 13
        d4 e4 f4 e8 [ d8 ] | % 14
        | % 14
        e4. d8 c4 e4 | % 15
        | % 15
        f1 | % 16
        g,8 [ f8 ] f2. | % 17
        | % 17
        r4 a8 [ b8 ] cis8 [ d8 e8 f8 ] | % 18
        | % 18
        e8. [ d16 ] d2. | % 19
        | % 19
        r4 d8 [ d8 ] c8 [ bes8 a8 g8 ] | \barNumberCheck #20
        | \barNumberCheck #20
        bes4 a8 [ a8 ~ ] a4 g4 | % 21
        | % 21
        f4 a8 [ g8 ~ ] g4. d8 | % 22
        | % 22
        f4 a8 [ a8 ~ ] a2 }
    | % 23
    | % 23
    f4 a4 g4 d4 | % 24
    | % 24
    f4 a8 [ a8 ~ ] a2 ^\fermata \bar "|."
    }

PartPOneVoiceOneChords =  \chordmode {
    | % 1
    | % 1
    f8:5 | % 2
    | % 2
    s8*7 e4:m7 s4 a8:7 | % 3
    | % 3
    s4. d8.:m5 s16 d2.:m5/+c | % 4
    | % 4
    bes4:5 s4 c8:7 | % 5
    | % 5
    s4. f4:5 s2 c4:5/+e | % 6
    | % 6
    d4:m5 s4 g4.:5 | % 7
    | % 7
    s8 bes4:5 f8:5 | % 8
    | % 8
    s8*5 a2:11 a2:7 | % 9
    | % 9
    d4:m5 c4:5 bes4:5 d8:m5/+a | \barNumberCheck #10
    | \barNumberCheck #10
    s8 g4.:m5 s8 c4:5 | % 11
    | % 11
    s4 f1:5 | % 12
    | % 12
    a2:11 a2:7 | % 13
    | % 13
    d4:m5 c4:5 bes4:5 d8:m5/+a | % 14
    | % 14
    s8 g4.:m5 s8 c4:5 | % 15
    | % 15
    s4 f1:5 | % 16
    | % 17
    | % 17
    s1 e4:m7 s4 a8:7 | % 18
    | % 18
    s4. d8.:m5 s16 d2.:m5/+c | % 19
    | % 19
    bes4:5 s4 c8:5 | \barNumberCheck #20
    | \barNumberCheck #20
    s4. f4:5 s2 c4:5/+e | % 21
    | % 21
    d4:m5 s4 g4.:5 | % 22
    | % 22
    s8 bes4:5 f8:5 | % 23
    | % 23
    s8*5 f4:5/+c s4 g4:5/+b | % 24
    | % 24
    s4 bes4:5 f8:5 }

PartPOneVoiceOneLyricsOne =  \lyricmode { Yes -- day, all trou -- far
    way, Now looks here to Oh I be -- in Yes -- ter Why she had to go I
    know, she would -- "n't" say. I said some -- thing wrong, now long
    for Yes -- ter -- day. Yes -- day, love such game play Now need hide
    a -- Oh I be -- in Yes -- ter -- Mm mm mm mm mm mm }
PartPOneVoiceOneLyricsTwo =  \lyricmode { Sud -- ly, "I'm" half used be.
    "There's" shad -- o -- ver Oh Yes -- ter -- came sud -- den --
    \skip4 \skip4 \skip4 \skip4 \skip4 \skip4 \skip4 \skip4 \skip4
    \skip4 \skip4 \skip4 \skip4 \skip4 \skip4 \skip4 \skip4 \skip4
    \skip4 \skip4 \skip4 \skip4 \skip4 \skip4 \skip4 \skip4 \skip4
    \skip4 \skip4 \skip4 \skip4 \skip4 \skip4 \skip4 \skip4 \skip4
    \skip4 \skip4 \skip4 \skip4 \skip4 \skip4 \skip4 \skip4 }

% The score definition
\new Staff <<
    \context ChordNames = "PartPOneVoiceOneChords" \PartPOneVoiceOneChords
    \context Staff <<
        \context Voice = "PartPOneVoiceOne" { \PartPOneVoiceOne }
        \new Lyrics \lyricsto "PartPOneVoiceOne" \PartPOneVoiceOneLyricsOne
        \new Lyrics \lyricsto "PartPOneVoiceOne" \PartPOneVoiceOneLyricsTwo
        >>
    >>

