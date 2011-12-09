
\version "2.12.3"
% automatically converted from yest.xml

\header {
    poet = "#{poet}"
    tagline = DoremiScript
    encodingdate = "#{encoding_date}"
    composer = "#{composer}"
    encodingsoftware = DoremiScript
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
    r4 a8 [ b8 ] cis8 [ d8 e8 f8 ] }

PartPOneVoiceOneChords =  \chordmode {
    | % 1
    | % 1
    f8:5 | % 2
    | % 2
    s8*7 e4:m7 s4 a8:7 }

PartPOneVoiceOneLyricsOne =  \lyricmode { Yes -- day, all trou -- }
PartPOneVoiceOneLyricsTwo =  \lyricmode { Sud -- ly, "I'm" half }

% The score definition
\new Staff <<
    \context ChordNames = "PartPOneVoiceOneChords" \PartPOneVoiceOneChords
    \context Staff <<
        \context Voice = "PartPOneVoiceOne" { \PartPOneVoiceOne }
        \new Lyrics \lyricsto "PartPOneVoiceOne" \PartPOneVoiceOneLyricsOne
        \new Lyrics \lyricsto "PartPOneVoiceOne" \PartPOneVoiceOneLyricsTwo
        >>
    >>

