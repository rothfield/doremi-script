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
r4 c'8[ c'8] \bar "|"  \break        
 r4 r8[ c'8] \bar "|"  \break        
 c'1 \bar "|"  \break        
 c'4~[ c'8] d'8 \bar "|"  \break        
 c'2~[ c'8] d'8 \bar "|"  \break        
 c'2. d'2. \bar "|"  \break        
 c'1 \bar "|" d'1 \bar "|"  \break        
 c'1~ \bar "|" c'1 \bar "|"  \break        
 r8[ c'8~ c'4] d'4~[ d'8] e'8~ \bar "|" e'4 f'4~[ f'8] g'8~[ g'4] \bar "|"  \break        

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
