\version "2.12.3"
melody = \relative c' {
		\clef treble
				\key c \major
				\time 4/4

\bar "|:"				a4 b c d
}

text = \lyricmode {
		Aaa Bee Cee Dee
}

\score{
		<<
				\new Voice = "one" {
						\autoBeamOff
								\melody
				}
		\new Lyrics \lyricsto "one" \text
				>>
				\layout { }
		\midi { }
}

