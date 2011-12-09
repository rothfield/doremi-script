
\version "2.12.3"
% automatically converted from happy.xml

\header {
    }

\layout {
    \context { \Score
        autoBeaming = ##f
        }
    }
PartPOneVoiceNone =  \relative c' {
    \clef "mezzosoprano" \key c \major \numericTimeSignature\time 4/4 c1
    }


% The score definition
\new Staff <<
    \set Staff.instrumentName = "Music"
    \context Staff << 
        \context Voice = "PartPOneVoiceNone" { \PartPOneVoiceNone }
        >>
    >>

