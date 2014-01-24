#(ly:set-option 'midi-extension "mid")
\version "2.12.3"
\include "english.ly"
\header{ 
title = ""
composer = ""
  tagline = ""  % removed 
}
%{
 -- SS |

-- -S |

S - - - | 

S -R |

S -- -R |

S - - R - - |

S - - - | R - - - |

S - - - | - - - - |

-S -- R- -G | -- m- -P -- | 
 %}
  
melody = {
\once \override Staff.TimeSignature #'stencil = ##f
\clef treble
\key c \major
\autoBeamOn  
\cadenzaOn
r4  c'8 c'8 \bar "|"  \break        \grace s64 
 r4  r8 c'8 \bar "|"  \break        \grace s64 
 c'1~    \bar "|"  \break        \grace s64 
 c'4~ c'8  d'8 \bar "|"  \break        \grace s64 
 c'2~ c'8    d'8 \bar "|"  \break        \grace s64 
 c'2.~   d'2.   \bar "|"  \break        \grace s64 
 c'1~    \bar "|" d'1    \bar "|"  \break        \grace s64 
 c'1~    \bar "|" c'1    \bar "|"  \break        \grace s64 
 r8 c'8 c'4   d'4 d'8   e'8~~ \bar "|" e'4  f'4 f'8   g'8 g'4   \bar "|"  \break        \grace s64 

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
    \remove "Bar_number_engraver"
  } 
  }
\midi { 
  \context {
    \Score
    tempoWholesPerMinute = #(ly:make-moment 200 4)
   }
 }
}
