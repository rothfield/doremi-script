(ns doremi_script_clojure.to_lilypond
  (:require	
    [clabango.parser :refer [render]]
    [clojure.java.io :refer [resource]]
    [clojure.string :refer [trim join upper-case lower-case]] 
    [clojure.pprint :refer [pprint]] 
    [clojure.walk :refer [postwalk
                          postwalk-replace keywordize-keys]]
    ))

(comment
  (use 'clojure.stacktrace) 
  (print-stack-trace *e)
  )

(defn- my-seq[x]
  "seq through the data structure, which is like"
  " {:items [ {:items [1 2 3]} 2 3]}"
  "Don't include items like {:items [1 2 3]} "
  "just [1 2 3]"
  ;; TODO: redo
  ;;(filter #(not (:items %))
  (tree-seq
    (fn branch?[x] (or (vector? x) (map? x))) 
    (fn children[y] 
      (cond 
        (and (map? y) (:items y)) 
        (:items y)
        (and (map? y) (:lines y)) 
        (:lines y)
        (vector? y)
        (rest y)))
    x))

(def debug false)

(defn get-attribute[item attribute]
  (some #(if (= attribute (:my_type %)) %) (:attributes item)))

(def lilypond-symbol-for-tie "~" )
;; A tie is created by appending a tilde ~ to the first note being tied.
;;
;; g4~ g c2~ | c4~ c8 a~ a2 |
;;
(def lilypond-invisible-grace-note 
  ;; s is short for spacer  
  (str "\\" "grace s64"))

(def tick "'")

(def comma ",")

(defn octave-number->lilypond-octave[num]
  ;; Middle c is c'
  (cond (nil? num)
        tick
        (>= num 0)
        (apply str (take (inc num) (repeat tick)))
        true
        (apply str (take (dec (- num)) (repeat comma)))))

(def normalized-pitch->lilypond-pitch
  ;; TODO: double sharps and flats, half-flats ??"
  ;; includes dash (-) -> r "
  {
   "-" "r", "C" "c", "C#" "cs", "Cb" "cf", "Db" "df", "D" "d", "D#" "ds",
   "Eb" "ef", "E" "e", "E#" "es", "F" "f", "Fb" "ff", "F#" "fs", "Gb" "gf",
   "G" "g", "G#" "gs", "Ab" "af", "A" "a", "A#" "as", "Bb" "bf", "B" "b",
   "B#" "bs", })

(def grace-note-pitch-template
  (-> "lilypond/grace_note_pitch.tpl" resource slurp trim))

(defn lilypond-grace-note-pitch[pitch]
  "generate a single pitch for use as a grace note"
  (render grace-note-pitch-template 
          {:lilypond-pitch 
           (normalized-pitch->lilypond-pitch (:normalized_pitch pitch))
           :lilypond-octave
           (octave-number->lilypond-octave (:octave pitch))
           :duration
           "32"
           }))

(def lilypond-beam-start "[")

(def lilypond-beam-end "]")

(defn beam-notes[ary]
  (if  (< (count ary) 2)
    ary
    (let [ary2 (into [] ary)]
      (assoc ary2 
             0
             (str (first ary2) lilypond-beam-start)
             (dec (count ary2))  
             (str (last ary2) lilypond-beam-end)))))

(defn lilypond-grace-notes[ornament]
  ;;      #  c1 \afterGrace d1( { c16[ d]) } c1
  ;;      #  In the above line, generate what is between {}
  (->> ornament 
       :ornament_items 
       (map lilypond-grace-note-pitch)
       beam-notes
       (join " ")))

(defn get-ornament[pitch]
  (get-attribute pitch :ornament))

(defn ratio-to-lilypond[my-numerator subdivisions-in-beat]
  { :pre [ (integer? my-numerator)
          (integer? subdivisions-in-beat)]
   :post [ (vector? %)] 
   }

  (let [my-ratio (/ my-numerator subdivisions-in-beat)]
    ;; In the case of beats whose subdivisions aren't powers of 2, we will
    ;; use a tuplet, which displays, for example, ---3---  above the beat
    ;; if the beat has 3 microbeats.
    ;; Take the case of  S---R. beat is subdivided into 5.  Use sixteenth notes. 4 for S and 1 for R. 5/16 total.  
    ;; For subdivision of 3, use 3 1/8 notes.
    ;; For subdivision of 5 use 5 1/16th notes.
    ;; For 6 use   16th notes
    ;; etc
    ;; For over 32 use 32nd notes, I guess.
    ;; confusing, but works
    ;; Things like S---r should map to quarter note plus sixteenth note in a 5-tuple
    ;; Take the case of S----R--   
    ;; S is 5 microbeats amounting to 5/32nds. To get 5 we have to tie either
    ;; 4/8th of a beat plus 1/32nd  or there are other approaches.
    (if (not (ratio? my-ratio))
      ({ 1 ["4"]
        2 ["2"]
        3 ["2."]
        4 ["1"]  ;; review
        8 ["1" "1"]
        } my-ratio
       (into [] (repeat my-numerator "4"))
       )
      ;; else
      (let [ 
            my-table
            { 1 ["4"] ;; a whole beat is a quarter note
             (/ 1 2) ["8"] ;; 1/4 of a beat is 16th
             (/ 1 4) ["16"] ;; 1/4 of a beat is 16th
             (/ 1 8) ["32"] ;; 1/8th of a beat is a 32nd. 
             (/ 1 16) ["64"] ;; 1/16th of a beat is 64th. 16/64ths=beat
             (/ 1 32) ["128"] ;; 32nd of a beat is 128th note
             (/ 3 4) ["8."] ;; 3/4 of a beat is  dotted eighth
             (/ 3 8) ["16."] ;; 
             (/ 3 16) ["32."] ;;  1/32 + 1/64 = 3/64 =3/16th of beat = 3/64 dotted 32nd
             (/ 3 32) ["64."]
             (/ 3 64) ["128."]
             (/ 5 4) ["4" "16"] ;; 1 1/4 beats= quarter tied to 16th
             (/ 5 8) ["8" "32"]
             (/ 5 16) ["16" "64"];;
             (/ 5 32) ["32" "128"];;
             (/ 5 64) ["64" "256"];;
             (/ 5 128) ["128" "512"];;
             (/ 7 4) ["4" "8."] ;;
             (/ 7 8) ["8.."] ;; 1/2 + 1/4 + 1/8  
             (/ 7 16) ["16" "32."] ;; 1/4+ 3/16   
             (/ 7 32) ["64" "128."] ;;   
             (/ 11 16) ["8" "64."] ;; 1/2 + 

             } 
            ;;  
            new-denominator 
            (cond (#{1 2 4 8 16 32 64 128 256 512} subdivisions-in-beat)
                  subdivisions-in-beat
                  (= 3 subdivisions-in-beat) 
                  2 
                  (<  subdivisions-in-beat 8)
                  4 
                  (< subdivisions-in-beat 16)
                  8 
                  (< subdivisions-in-beat 32)
                  16 
                  (< subdivisions-in-beat 64)
                  32 
                  true
                  32 
                  )
            new-ratio (/ my-numerator new-denominator)
            ]
        (get my-table new-ratio 
             [
              (str "unsupported: " my-numerator "/" new-denominator)
              ]) 
        ))))

(def mordent-snippet
  "\\mordent") 

(defn get-chord[pitch]
  (get-attribute pitch :chord_symbol))

(defn chord-snippet[chord]
  { :pre [ (or (nil? chord) (= :chord_symbol (:my_type chord)))   ] }
  (if (nil? chord)
    ""
    ;; else
    (str "^\"" (:source chord) "\"")))


(defn lilypond-escape[x]
  ;; (println "x is " x)
  ;;      # Anything that is enclosed in %{ and %} is ignored  by lilypond
  ;;      composition-data.source="" if !composition-data.source?
  ;;      src1= composition-data.source.replace /%\{/gi, "% {"
  ;;      src= src1.replace /\{%/gi, "% }"
  (clojure.string/replace x #"\{%" "% {")
  ) 

(defn- my-seq2[x]
  (tree-seq (fn branch[node](or (map? node) (vector? node)))
            (fn children[node]
              (or (:items node) (:lines node) (:attributes node)
                  (:attributes node) node))
            x))


(defn barline->lilypond-barline[barline-type]
  " maps barline-type field for barlines"
  (let [my-map
        {
         :reverse_final-barline "\\bar \".|\""
         :final_barline "\\bar \"|.\" "
         :double_barline "\\bar \"||\" " 
         :single_barline "\\bar \"|\"" 
         :left_repeat "\\bar \"|:\"" 
         :right_repeat "\\bar \":|\"" 
         }
        ]
    (or (barline-type my-map) (:single-barline my-map))
    ))


(def pitch-template (-> "lilypond/pitch.tpl" resource slurp trim))

(def rest-template (-> "lilypond/rest.tpl" resource slurp trim))

(def before-ornament-template
  (-> "lilypond/before_ornament.tpl" resource slurp trim))

(def ending-template
  (-> "lilypond/ending.tpl" resource slurp trim))

(def tied-pitch-template
  (-> "lilypond/tied_pitch.tpl" resource slurp trim))

(def lilypond-after-ornament-directive "\\afterGrace ")

(defn whole-note?[x]
  (= x {:numerator 1, :denominator 1}))

(defn extra-tied-whole-notes[pitch]
  (let [whole-note-count 
        (count (filter whole-note? (:fraction_array pitch)))]
    (if  (= 0 whole-note-count)
      nil
      {:numerator whole-note-count :denominator 1})))

(defn combine-whole-notes-in-fraction-array[ary]
  ;; Problem when the whole notes go into next measure!!!
  (let [z (partition-by identity ary)]
    (into [] (flatten (map #(if (whole-note? (first %))
                              {:numerator (count %) :denominator 1}
                              %) z)))))

(def x [{:numerator 1, :denominator 1}{:numerator 1, :denominator 1} {:numerator 1, :denominator 3}{:numerator 1, :denominator 3} {:numerator 1, :denominator 1} ])

(if false
  (pprint (combine-whole-notes-in-fraction-array x)))

(defn draw-pitch-with-one-duration[pitch duration]
  { :pre [  (= (:pitch (:my_type pitch)))
            (:numerator pitch)
          (string? duration)
          (not= duration "")
         (:denominator pitch) 
          ]
   :post [ (string? %)]  
   }
   (str (normalized-pitch->lilypond-pitch (:normalized_pitch pitch))
        (octave-number->lilypond-octave (:octave pitch))
         duration 
        )
  )


(defn draw-pitch[pitch beat-subdivisions beam-start-or-end]
  { :pre [  (#{:pitch :dash}  (:my_type pitch)) 
          (integer? beat-subdivisions) 
          (contains? #{nil lilypond-beam-start lilypond-beam-end}
                     beam-start-or-end)
          ]
   :post [ (string? %)] 
   }
  "Render a pitch/dash as lilypond text. Renders tied notes as needed"
  ;; "Rests are never tied"
  ;; Use fraction-array to draw the tied notes
  ;; In draw-pitch for the Sa in the following:  
  ;; -S -- -- -R
  (if false (do
              (println "entering draw-pitch- pitch is")
              (pprint pitch)
             (println "\n\n")))
  ;;; {:fraction_array [{:numerator 1, :denominator 2}],
  ;;;  :numerator 1,
  ;;;   :start_index 3,
  ;;;    :denominator 2,
  ;;;     :beat-counter 0,
  ;;;      :my_type :dash,
  ;;;       :case3 true,
  ;;;        :pitch-counter 1,
  ;;;         :pitch_to_use_for_tie {:pointer true},
  ;;;          :dash_to_tie true}
  ;;;
  ;;;
  ;;;
  ;;;
  ;;;
  ;;;




  ;;;     {:fraction_array [{:numerator 1, :denominator 1}],
  ;;;      :numerator 1,
  ;;;       :start_index 12,
  ;;;        :denominator 1,
  ;;;         :beat-counter 0,
  ;;;          :my_type :dash,
  ;;;           :case3 true,
  ;;;            :pitch-counter 5,
  ;;;             :pitch_to_use_for_tie
  ;;;              {:syllable nil,
  ;;;                :chord nil,
  ;;;                  :fraction_array [{:numerator 1, :denominator 1}],
  ;;;                    :pitch_source "S",
  ;;;                      :normalized_pitch "C",
  ;;;                        :numerator 1,
  ;;;                          :start_index 8,
  ;;;                            :denominator 1,
  ;;;                              :pointer true,
  ;;;                                :octave 0,
  ;;;                                  :my_type :pitch,
  ;;;                                    :attributes [],
  ;;;                                      :pitch-counter 3,
  ;;;                                        :column_offset 0,
  ;;;                                          :tala nil,
  ;;;                                            :value :S},
  ;;;                                             :dash_to_tie true}
  ;;;
  ;;;
  ;;;
  ;;;
  ;;; donot skip this one:
  ;;; {:fraction_array [{:numerator 1, :denominator 2}],
  ;;;  :numerator 1,
  ;;;   :start_index 3,
  ;;;    :denominator 2,
  ;;;     :beat-counter 0,
  ;;;      :my_type :dash,
  ;;;       :case3 true,
  ;;;        :pitch-counter 1,
  ;;;         :pitch_to_use_for_tie {:pointer true},
  ;;;          :dash_to_tie true}
  ;;;
  ;;;
  ;;;
  ;; aargh!!!!
  (cond (:ignore pitch)
        ""
        ;; (and  (:dash_to_tie pitch) (= :dash (:my_type pitch)))
        ;; ""
        (and  (:tied pitch) (not= :pitch (:my_type pitch)))
        ""
        (and (:dash_to_tie pitch) (:octave (:pitch_to_use_for_tie pitch)))
        ""
        ;;(and  (:dash_to_tie pitch) (= :dash (:my_type pitch))))
        ;; (and (= :dash (:my_type pitch)) (not (:dash_to_tie pitch))
        ;;     (not (:rest pitch)))
        true
        (let
          [ 
           debug false ;;(= 1 (:start_index pitch)) 
           _ (if debug (pprint pitch))
           ornament (get-ornament pitch)
           placement (:placement ornament)
           has-after-ornament (= :after placement)
           has-before-ornament (= :before placement)    
           begin-slur (if (get-attribute pitch :begin_slur) "(" )
           lilypond-octave  (octave-number->lilypond-octave (:octave pitch))

           combined-fraction-array 
           (combine-whole-notes-in-fraction-array (:fraction_array pitch))
           _ (if debug (do 
                         (println "combined fraction array:")
                         (pprint combined-fraction-array)))
           needs-tie (or (:tied pitch)
                         (> (count combined-fraction-array) 1))
           _ (if debug (println "needs-tie" needs-tie))
           durations-for-first-note 
           (ratio-to-lilypond 
             (:numerator (first combined-fraction-array)) 
             (:denominator (first combined-fraction-array)))
           _ (if debug (do
                         (println "durations-for-first-note:" durations-for-first-note)
                         (println)
                         (println "before setting durations2")
                         (pprint combined-fraction-array)
                         ))
           durations2 (flatten (map #(ratio-to-lilypond (:numerator %)
                                                        (:denominator %)) 
                                    (rest combined-fraction-array)))
           _ (if debug (do (println "durations2") (pprint durations2)))
           duration (first durations-for-first-note) 
           chord (chord-snippet (get-chord pitch))
           ending (:source (get-attribute pitch :ending))
           ending-snippet (if ending
                            (render ending-template { :ending 
                                                     (:source (get-attribute pitch :ending))
                                                     }))
           pitch2 (if (and (= :dash (:my_type pitch)) 
                           (:dash_to_tie pitch))
                    (assoc pitch :normalized_pitch 
                           (get-in pitch [:pitch_to_use_for_tie :normalized_pitch] ) 
                           :octave (get-in pitch [:pitch_to_use_for_tie :octave] ))
                    ;; else
                    pitch)
           lilypond-octave  (octave-number->lilypond-octave (:octave pitch2))
           lilypond-pitch 
           (normalized-pitch->lilypond-pitch (:normalized_pitch pitch2))
           all-durations (concat (rest durations-for-first-note) durations2)   
           _ (if debug (println "all-durations"))
           _ (if debug (pprint all-durations))
           extra-tied-durations ;;; durations within current beat!
           (if (> (count all-durations) 0)
             (str lilypond-symbol-for-tie
                  (join lilypond-symbol-for-tie
                        (map (fn[duration] 
                               (render tied-pitch-template
                                       {
                                        ;; TODO: doesn't make sense!!!
                                        ;; should only apply to last!! ???
                                        :beam-start-or-end beam-start-or-end
                                        :duration duration
                                        :lilypond-pitch lilypond-pitch
                                        :lilypond-octave lilypond-octave
                                        }
                                       ))
                             all-durations
                             ))))
           ]
          (cond (:ignore pitch2)
                ""
                (:pointer pitch2)
                ""
                (and (= :dash (:my_type pitch2)) 
                     (:dash_to_tie pitch2))
                (render rest-template { :duration duration
                                       :chord chord
                                       :ending ending-snippet                
                                       })
                (and (= :dash (:my_type pitch2)) 
                     (not (:dash_to_tie pitch2)))
                (render rest-template { :duration duration
                                       :chord chord
                                       :ending ending-snippet                
                                       })
                (or (= :pitch (:my_type pitch2))
                    (= :dash (:my_type pitch2)))
                (render pitch-template 
                        {
                         :before-ornament-snippet 
                         (if has-before-ornament
                           (render before-ornament-template 
                                   {:grace-notes  
                                    (lilypond-grace-notes ornament) }))
                         :after-ornament-directive
                         (if has-after-ornament 
                           lilypond-after-ornament-directive) 
                         :lilypond-pitch lilypond-pitch
                         :lilypond-octave lilypond-octave
                         :duration duration
                         :beam-start-or-end beam-start-or-end
                         :lilypond-symbol-for-tie 
                         (if (and needs-tie 
                                  ;;and (:tied pitch)
                                  (not has-after-ornament)) 
                           lilypond-symbol-for-tie )
                         :mordent (if (get-attribute pitch2 :mordent) 
                                    mordent-snippet)
                         :begin-slur begin-slur
                         :extra-end-slur "" ;;; TODO 
                         :end-slur (if (get-attribute pitch2 :end_slur) ")" )
                         :ending ending-snippet                  
                         :chord chord
                         :after-ornament-contents 
                         (if has-after-ornament
                           (str " { " (lilypond-grace-notes ornament ) " }"))
                         :extra-tied-durations extra-tied-durations
                         }
                        )))))
(def lilypond-break  "\\break\n")

(defn beat-is-all-dashes?[beat] 
  { :pre [  (= (:my_type "beat"))  ]}
  (not-any? #(= :pitch (:my_type %)) (:items beat)))

(def beat-tuplet-template 
  (-> "lilypond/beat_with_tuplet.tpl" resource slurp trim))

(defn tuplet-numerator-for-odd-subdivisions[subdivisions-in-beat]
  ;; fills in numerator for times. For example
  ;; \times ???/5 {d'16 e'8 d'16 e'16 }
  ;; The ??? should be such that 5/16 *  ???/5 =  1/4
  ;; So ??? = 4
  ;; TODO: dry with duration code
  (cond (= 3 subdivisions-in-beat) 
        2 
        (<  subdivisions-in-beat 8)
        4 
        (< subdivisions-in-beat 16)
        8 
        (< subdivisions-in-beat 32)
        16 
        (< subdivisions-in-beat 64)
        32 
        true
        32 
        )
  )

(defn draw-beat[beat]
  (if false (do (println "\n\n****draw-beat:") (pprint beat) (println "\n\n\n")))
  ;; Manually beam beat as follows: SR -> c'[ d']  
  ;; Only if more than one pitch in the beat of course
  ;; But don't beam if there is a quarter note which can be the
  ;; case with S-R
  (let [
        ;; Use pitch-ids to identify first and last pitches
        pitch-ids (map :pitch-counter 
                       (filter 
                         #(and (#{ :dash :pitch } (:my_type %))
                               (:pitch-counter %))
                         (:items beat)))
        subdivisions (:subdivisions beat)
        first-pitch (some #(if (= :pitch (:my_type %)) %) (:items beat))
        ;; TODO: rewrite. Should deal with durations
        do-not-beam-case (and (#{3 5 7 9} subdivisions) 
                              (some 
                                (fn[pitch] 
                                  (and
                                    (#{:pitch :dash}  (:my_type pitch))
                                    (not (:ignore pitch))
                                    (> 
                                      (/ (:numerator pitch)
                                         (:denominator pitch))
                                      (/ 1 2))))
                                (:items beat))) 
        beamable? 
        (and (> (count pitch-ids) 1)
             (not do-not-beam-case))
        beat-ary 
        (map (fn draw-beat-item[ item]
               (if false (do
               (println "draw-beat-item, item is ")
               (pprint item)))
               (let [
                     ;; If this is the first pitch or tied rest and there are
                     ;; more than one in the beat, add [ to end of note to
                     ;; indicate beam start.
                     beam-start-or-end
                     (cond (and beamable?
                                (= (first pitch-ids) (:pitch-counter item)))
                           lilypond-beam-start 
                           (and beamable?      
                                (= (last pitch-ids) (:pitch-counter item)))
                           lilypond-beam-end)
                     my-type (:my_type item)]
                 (cond (#{:pitch :dash} my-type)
                       (draw-pitch item (:subdivisions beat)
                                   beam-start-or-end)
                       )))
             (:items beat))
        beat-content (join " " beat-ary)
        ]
    (if (and (not (#{0 1 2 4 8 16 32 64 128} (:subdivisions beat)))
             (not (beat-is-all-dashes? beat)))
      (render beat-tuplet-template {:beat-content  beat-content
                                    :tuplet-numerator 
                                    (tuplet-numerator-for-odd-subdivisions (:subdivisions beat))
                                    :subdivisions (:subdivisions beat)
                                    })
      ;; else
      beat-content)     
    ))

(defn draw-barline[barline]
  (barline->lilypond-barline (:my_type barline)))

(defn draw-measure[measure]
  (if false (do (println "\n\ndraw-measure") (pprint measure) (println "\n\n\n")))
  { :pre [(= :measure (:my_type measure))]} 
  (join " "
        (map 
          (fn draw-measure-item[item]
            (let [my-type (:my_type item)]
              (cond 
                (= :beat my-type)
                (draw-beat item)
                )))
          (:items measure)
          )))

(def draw-line-break
  ;; Lilypond text for line break.
  ;; Lilypond doesn't like  left-repeat break barline
  ;; That is why I insert an invisible grace note spacer after the
  ;; break. Otherwise lilypond will combine the left-repeat and barline
  ;; and you lose the left repeat.
  ;; Probably better to examine bars at end of line and beginning of next
  ;; line and combine them into one.
  (str " \\break        " lilypond-invisible-grace-note " \n"))

(defn draw-line[line]
  (if false (do
  (println "draw-line. Line is:")
  (pprint line)
             (println "\n\n\n")
    
    ))
  ;; line consists of items consisting of
  ;; optional :line_number followed by :barline :measure :measure etc "
  ;; We don't render line numbers in lilypond. (yet)"
  (let [
        first-item (first (:items line))
        my-items (if (= :line_number (:my_type first-item))
                   (rest (:items line))
                   (:items line))

        ] 
    (if false (do (println "\n\n\ndraw-line, my-items") (pprint my-items) (println "------>")))
    (join " " 
          (map (fn draw-line-item[item]
                 (let [my-type (:my_type item)]
                   (cond (= :measure my-type)
                         (draw-measure item)
                         (:is_barline item)
                         (draw-barline item)
                         )))
               my-items))))


(defn is-abc-line[line]
  (if (nil? (:kind line))
    false
    ;; else
    (re-find #"abc" (:kind line))))

(defn notation-is-in-abc[composition-data]
  (some is-abc-line (:lines composition-data)))

(def transpose-template
  (-> "lilypond/transpose.tpl" resource slurp trim))

(def time-signature-template
  (-> "lilypond/time_signature.tpl" resource slurp trim))

(def omit-time-signature-snippet
  "Returns a lilypond snippet that prevents printing of the time signature"
  (-> "lilypond/omit_time_signature.txt" resource slurp trim))

(defn transpose-snippet[composition-data]
  "return transpose snippet for lilypond"
  "Don't transpose non-sargam."
  (let [my-key (:key composition-data)]
    (cond 
      (nil? my-key) 
      ""
      (= "c" (lower-case (:key composition-data)))
      ""
      (notation-is-in-abc composition-data)
      ""
      true
      (render transpose-template 
              {:key 
               (normalized-pitch->lilypond-pitch 
                 (upper-case (:key composition-data)))
               }
              ))))

(defn extract-lyrics[x]
  (map :syllable 
       (filter #(and (not (:pointer %)) (#{:pitch :dash} (:my_type %))
                     (:syllable %))
               (my-seq2 x))))

(def composition-template
  (-> "lilypond/composition.tpl" resource slurp trim))

(def key-template
  (-> "lilypond/key.tpl" resource slurp trim))

(def is-line? #{:sargam_line})
(comment
  (use 'clojure.stacktrace) 
  (print-stack-trace *e)
  )


(defn to-lilypond[doremi-data]
  {:pre  [(map? doremi-data)
          (= :composition (:my_type doremi-data))
          ]
   :post [ (string? %)]
   }
 ;; (pprint doremi-data)
  "Takes parsed doremi-script and returns lilypond text"
  (render composition-template 
          {:transpose-snip 
           (transpose-snippet doremi-data) 
           :extracted-lyrics
           (apply str (join " " (extract-lyrics doremi-data)))
           :beats-per-minute 
           200
           :title 
           (:title doremi-data)
           :author 
           (:author doremi-data)
           :src-snippet 
           (str  "%{\n " (lilypond-escape (:source doremi-data)) " \n %}\n")
           :time-signature-snippet 
           (if (:time_signature doremi-data)
             (render time-signature-template 
                     {:time-signature (:time_signature doremi-data)})
             ;; else
             omit-time-signature-snippet) 
           :key-snippet 
           (render key-template 
                   { :key "c"  
                    ;; Always set key to c !! 
                    ;; Transpose is used to move it to the right key
                    :mode (lower-case (:mode doremi-data "major"))
                    }) 
           :notes
           (join draw-line-break (map draw-line (:lines doremi-data))) 
           }))





(defn pprint-composition[node]
  ;; (pprint node)
  ;; (spit "f.txt" (with-out-str 
  (pprint (postwalk (fn[node] (cond 
                                (= :pitch (:my_type node))
                                (let [attributes (:attributes node)
                                      ;;   _ (println ":pitch case")
                                      ;;  _ (println "attributes are" attributes)

                                      attrs (if (get-attribute node :ornament) 
                                              {:ornament (get-attribute node :ornament)} ;;(:ornament (:attributes node))} 
                                              nil)
                                      base [(:value node) (:octave node) (:syllable node) ]
                                      ]
                                  ;;    (println "pitch node is"  node)
                                  ;; (println "attrs is" attrs)
                                  (if attrs
                                    (conj base attrs)
                                    base))
                                (and (map? node) (:ornament_items node))
                                node
                                ;;   (do (pprint node)
                                ;;      [ (:my_type node) (:ornament_items node)])
                                (and (map? node) (:items node))
                                [ (:my_type node) (:items node)]
                                (and (map? node) (:lines node))
                                [ (:my_type node) (:lines node)]
                                (:my_type node)
                                (:my_type node)
                                true
                                node)) 
                    node)))

(def runtest false)

(if false
  (pprint
    (doremi_script_clojure.core/doremi-text->parse-tree
      "GR\n  S |")))
;;srruby turn your record into a map like this: (into {}  (->aaa 1 2))
(if false
  (let [txt "| SR"]
      (println "txt is\n " txt)
  (println (to-lilypond 
             (doremi_script_clojure.core/doremi-script-text->parsed-doremi-script txt)
               ;; (-> "fixtures/yesterday.txt" resource slurp)
               
             ))))
(if false 
  (pprint
    (to-lilypond (doremi_script_clojure.core/doremi-script-text->parsed-doremi-script " | S - - - | - - - - "))))

;;;;;;;;;;;; For testing ;;;;;
(if nil 
  ;; (use 'clojure.stacktrace) 
  ;; (print-stack-trace *e)
  (println
    (to-lilypond 
      (doremi_script_clojure.core/doremi-script-text->parsed-doremi-script 
      ;;  " S - - - | - - R - "
        "S | -"
        ;;        "P - - - - - - - -"
        ;; (-> "fixtures/yesterday.txt" resource slurp)
        ;;  (-> "fixtures/aeolian_mode_without_key_specified.txt" resource slurp)
        ;;   " RGm\nS\n\n R\nS"
        ))

    )
  )


