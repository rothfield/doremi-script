  (let [ syllables (filter #(and (vector? %) (= (first %) :SYLLABLE)) nodes)
        ]
