\version "2.18.2"
\include "english.ly"
\header{ 
		title = "" 
				composer = "" 
}

\include "english.ly"

%{

		%}

		\score {
						\new Staff {
								\relative c' 
										\key c \major

										\time 4/4
										\autoBeamOn  
										{
												c'4 r4 r4 ef'4 f'4 | \break \grace s16  

										}
								\addlyrics 
								{  he- llo john
								}
		     }				
		}
		\score {

				\new Staff {
						\relative c' 
								\key c \major

								\time 4/4
								\autoBeamOn  
								{
										d'4 r4 r4 r4 \bar "|"  r4 r4 r4 r4 \bar "|" \break \grace s16 
								}
						\addlyrics 
						{  
								line two
						}
				}
		}

