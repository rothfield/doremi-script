\version "2.18.2"
\include "english.ly"
\header{ 
title = "Row row" 
composer = "" 
}

\include "english.ly"

%{
		
%}

\score {
		<<
\new Staff {
    \relative c' 
        \key c \major

        \time 4/4
        \autoBeamOn  
        {
              c'4 r4 r4 r4 \bar "|" \break \grace s16 

        }
    \addlyrics 
    {  he- llo
    }
}

\new Staff {
    \relative c' 
        \key c \major

        \time 4/4
        \autoBeamOn  
        {
              df'4 r4 r4 r4 \bar "|" \break \grace s16 

        }
    \addlyrics 
    {  
    }
}

>>
}
