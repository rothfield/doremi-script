(def sample-data [:composition
                  [:attribute-section "Title" "test semantic analyzer"]
                  [:sargam-stave
                   [:sargam-upper-octave-line [:mordent]]
                   [:sargam-upper-octave-line [:tala "+"] [:upper-octave-dot]]
                   [:sargam-upper-octave-line [:upper-upper-octave-symbol]]
                   [:sargam-upper-octave-line
                    [:upper-octave-dot]
                    [:sargam-ornament [:sargam-ornament-pitch "S"]]]
                   [:sargam-upper-octave-line [:upper-octave-dot]]
                   [:sargam-upper-octave-line [:chord "Dm7"]]
                   [:sargam-upper-octave-line
                    [:sargam-ornament
                     [:sargam-ornament-pitch "g"]
                     [:sargam-ornament-pitch "R"]
                     [:sargam-ornament-pitch "g"]]]
                   [:sargam-notes-line
                    [:sargam-measure [:sargam-beat
                                      [:sargam-pitch "g"]
                                      [:sargam-pitch "m"]

                                      ]]]
                   [:lower-octave-line [:lower-octave-dot]]
                   [:lyrics-line "hi-"]]]
  )
