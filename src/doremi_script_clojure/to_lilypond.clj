(ns doremi_script_clojure.to_lilypond
  (:require	
    [clabango.parser :refer [render]]
    [clojure.java.io :refer [resource]]
    [clojure.string :refer [trim join upper-case lower-case]] 
    [clojure.pprint :refer [pprint]] 
    ))

(comment
  (use 'clojure.stacktrace) 
  (print-stack-trace *e)
  )

(defn get-attribute[item attribute]
  (some #(if (= attribute (:my_type %)) %) (:attributes item)))

(def lilypond-symbol-for-tie "~" )

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
      ({ 1 "4"
        2 "2"
        3 "2."
        4 "1"  ;; review
        } my-ratio)
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
  (:pre (= :chord_symbol (:my_type chord)))
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

(defn draw-pitch[pitch beat-subdivisions beam-start-or-end]
  "Render a pitch/dash as lilypond text"
  (if (:ignore pitch)
    ""
    (let
      [ 
       ornament (get-ornament pitch)
       placement (:placement ornament)
       has-after-ornament (= :after placement)
       has-before-ornament (= :before placement)    
       begin-slur (if (get-attribute pitch :begin_slur) "(" )
       lilypond-octave  (octave-number->lilypond-octave (:octave pitch))
       durations (ratio-to-lilypond 
                   (:numerator pitch) 
                   beat-subdivisions)
       duration (first durations) 
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
       extra-tied-durations
       (if (> (count durations) 1)
         (str lilypond-symbol-for-tie
              (join lilypond-symbol-for-tie
                    (map (fn[duration] 
                           (render tied-pitch-template
                                   {
                                    :beam-start-or-end beam-start-or-end
                                    :duration duration
                                    :lilypond-pitch lilypond-pitch
                                    :lilypond-octave lilypond-octave
                                    }
                                   ))
                         (rest durations)    
                         ))))
       ]
      (cond (:ignore pitch2)
            ""
            (:pointer pitch2)
            ""
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
                     (if (and (:tied pitch)
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
  (:pre (= (:my_type "beat")))
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
  (:pre (= :measure (:my_type measure))) 
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
  ;; line consists of items consisting of
  ;; optional :line_number followed by :barline :measure :measure etc "
  ;; We don't render line numbers in lilypond. (yet)"
  (let [
        first-item (first (:items line))
        my-items (if (= :line_number (:my_type first-item))
                   (rest (:items line))
                   (:items line))

        ] 
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

(defn to-lilypond[doremi-data]
  :pre (map? doremi-data)
  :pre (= :composition (:my_type doremi-data))
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

;;;;;;;;;;;; For testing ;;;;;
(comment
;; (use 'clojure.stacktrace) 
;; (print-stack-trace *e)
(println
  (to-lilypond 
    (doremi_script_clojure.core/doremi-script-text->parsed-doremi-script 
     (-> "fixtures/aeolian_mode_without_key_specified.txt" resource slurp)
   ;;   " RGm\nS\n\n R\nS"
      ))

  )
)


