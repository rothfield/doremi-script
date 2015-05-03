\score {
		\new Staff <<
				\new Voice \relative c''' {
						\set midiInstrument = #"flute"
								\voiceOne
								\key g \major
								\time 2/2
								r2 g-"Flute" ~
								g fis ~
								fis4 g8 fis e2 ~
								e4 d8 cis d2
				}
		\new Voice \relative c'' {
				\set midiInstrument = #"clarinet"
						\voiceTwo
						b1-"Clarinet"
						a2. b8 a
						g2. fis8 e
						fis2 r
		}
		>>
				\layout { }
		\midi {
				\context {
						\Staff
				}
				\tempo 2 = 72
		}
}

