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

(def tick "'")
(def comma ",")
(def octave-number->lilypond-octave
  ;; Could also do: (apply str (take 2 (repeat tick))))
  ;;
  {
   -3 (str comma comma)
   -2 comma
   -1 ""
   0 tick
   1  (str tick tick) 
   2  (str tick tick)
   3  (str tick tick tick)
   })

(def normalized-pitch->lilypond-pitch
  ;; TODO: double sharps and flats, half-flats ??"
  ;; includes dash (-) -> r "
  {
   "-" "r"
   "C" "c"
   "C#" "cs"
   "Cb" "cf"
   "Db" "df"
   "D" "d"
   "D#" "ds"
   "Eb" "ef"
   "E" "e"
   "E#" "es"
   "F" "f"
   "Fb" "ff"
   "F#" "fs"
   "Gb" "gf"
   "G" "g"
   "G#" "gs"
   "Ab" "af"
   "A" "a"
   "A#" "as"
   "Bb" "bf"
   "B" "b"
   "B#" "bs"
   }
  )
(def grace-note-pitch-template
  (-> "lilypond/grace_note_pitch.tpl" resource slurp trim))

(defn lilypond-grace-note-pitch[pitch]
  "generate a single pitch for use as a grace note"
  (render grace-note-pitch-template 
          {:lilypond-pitch (normalized-pitch->lilypond-pitch (:normalized_pitch pitch))
           :lilypond-octave  (octave-number->lilypond-octave (:octave pitch))
           :duration "32"})
  )



(defn lilypond-grace-notes[ornament suppress-slurs]
  "TODO: deal with beaming and slurs"
  ;;      #  c1 \afterGrace d1( { c16[ d]) } c1
  ;;      #  In the above line, generate what is between {}
  (if (not (nil? ornament))
    (let [ary (into [] (map lilypond-grace-note-pitch (:ornament_items ornament)))
          needs-beam (> (count ary) 1)
          begin-slur (if (not suppress-slurs) "(")
          end-slur (if (not suppress-slurs) ")")
          begin-beam (if needs-beam "[")
          end-beam (if needs-beam "]")
          ary2 (assoc ary 0
                      (str (first ary) begin-slur begin-beam)
                      (dec (count ary))  
                      (str (last ary) end-slur end-beam))
          ]
      (join " " ary2))))


(defn get-ornament[pitch]
  (get-attribute pitch :ornament))

(defn zzgrace-notes[pitch in-slur]
  (if (not (get-ornament pitch))
    {:begin-slur false}
    ;; else
    (let [ ornament (get-ornament pitch)
          placement (:placement ornament)
          suppress-slurs (if (and (= :after placement)
                                  (or in-slur
                                      (:tied pitch)))
                           true)
          begin-slur (if (and (= :after placement)
                              (not in-slur))
                       "(")
          ]
      {:begin-slur begin-slur})))

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
        ;; (println "subdivisions in beat" subdivisions-in-beat)
        ;; (println "new-denominator is " new-denominator)
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

;; barline looks like this
(comment
  {:is_barline true,
   :my_type :single_barline,
   :source "|",
   :start_index 5}
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

(def partial-template
  (-> "lilypond/partial.tpl" resource slurp trim))
(def ending-template
  (-> "lilypond/ending.tpl" resource slurp trim))
(def tied-pitch-template
  (-> "lilypond/tied_pitch.tpl" resource slurp trim))

(def lilypond-after-ornament-directive "\\afterGrace ")

(defn draw-pitch[pitch in-slur beat-subdivisions beam-start-or-end]
  "Does alot. Not sure how to refactor."
  "Also handles dashes"
  "Render a pitch/dash as lilypond"
  (if (:ignore pitch)
    ""
    (let
      [ 
       ornament (get-ornament pitch)
       placement (:placement ornament)
       has-after-ornament (= :after placement)
       has-before-ornament (= :before placement)    

       suppress-slurs true
       old-suppress-slurs (and has-after-ornament
                               (or in-slur (:tied pitch)))

       begin-slur (if (get-attribute pitch :begin_slur) "(" )
       extra_end_slur  "" 
       ;;(if (and (:dash_to_tie pitch) 
       ;;                        has-after-ornament)
       ;;                ")")
       ;;begin-slur (if (and (= :after placement)
       lilypond-octave  (octave-number->lilypond-octave (:octave pitch))
       ;;_ (pprint pitch)
       duration-ary (ratio-to-lilypond 
                      (:numerator pitch) 
                      beat-subdivisions)
       duration (first duration-ary)
       ;; example grouping of 5 produces
       ;;      (/ 5 8) ["8" "32"]
       ;; _ (println "duration is: " duration)
       chord (chord-snippet (get-chord pitch))
       ending (:source (get-attribute pitch :ending))
       ending-snippet (if ending
                        (render ending-template { :ending 
                                                 (:source (get-attribute pitch :ending))
                                                 }))
       ; _ (println "pitch is")
       ; _ (pprint pitch)
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
       extra-tied-durations (if (> (count duration-ary) 1)
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
                                              (rest duration-ary)    
                                              ))))
       ]
      ;; this function returns the value of the following cond:
      (cond (:ignore pitch2)
            ""
            (:pointer pitch)
            ""
            (and (= :dash (:my_type pitch2)) 
                 (not (:dash_to_tie pitch2)))
            (render rest-template { :duration duration
                                   :chord chord
                                   :ending ending-snippet                
                                   })
            (or (= :pitch (:my_type pitch2))
                (= :dash (:my_type pitch2)))
            ;;
            ;;{{before-ornament-snippet}}{{after-ornament-start}}{{lilypond-pitch}}{{lilypond-octave}}{{duration}}{{lilypond-symbol-for-tie}}{{mordent}}{{begin-slur}}{{extra-end-slur}}{{end-slur}}{{ending}}{{chord}}{{after-ornament-contents}}
            (render pitch-template 
                    {
                     :before-ornament-snippet 
                     (if has-before-ornament
                       (render before-ornament-template {:grace-notes  
                                                         (lilypond-grace-notes ornament suppress-slurs) }))

                     :after-ornament-directive (if has-after-ornament 
                                                 lilypond-after-ornament-directive) 
                     :lilypond-pitch lilypond-pitch
                     :lilypond-octave lilypond-octave
                     :duration duration
                     :beam-start-or-end beam-start-or-end
                     :lilypond-symbol-for-tie 
                     (if (and (:tied pitch)
                              (not has-after-ornament)) 
                       lilypond-symbol-for-tie )
                     :mordent (if (get-attribute pitch2 :mordent) mordent-snippet)
                     :begin-slur begin-slur
                     :extra-end-slur "" ;;; TODO 
                     :end-slur (if (get-attribute pitch2 :end_slur) ")" )
                     :ending ending-snippet                  
                     :chord chord
                     :after-ornament-contents  (if has-after-ornament
                                                 (str " { " (lilypond-grace-notes ornament suppress-slurs) " }"))
                     :extra-tied-durations extra-tied-durations
                     }
                    )))))


(def lilypond-break  "\\break\n")
(def lilypond-beam-start "[")
(def lilypond-beam-end "]")

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
  ;;(println "entering draw-beat")
  ;;(pprint beat)
  "TODO: review durations. The durations will be a function of the beat subdivisions"
  "and the note count of pulses" 
  "Manually put in lilypond beaming rather than using autobeaming"
  "Previous version didn't beam 3,5, and 7 per beat. This version does"
  "One case that beaming produces a bad result is the following S-G"
  (let [
        ;; beam beat as follows: SR -> c'[ d']  
        ;; This is lilypond's manual beaming.
        ;; only beam if more than one beat.
        ;; get ids identifying the pitches
        ;; perhaps move this code down into pitch.tpl
        ;; The composition template sets autoBeam on. However, I seem to have found
        ;; that I need to manually beam each beat. Except, apparently for 3,5 and 7 per beat.
        ;; Get list of pitch ids. This gives us a count and helps identify the first
        ;; and last pitches.
        ;; _ (pprint (:items beat))
        pitch-ids (map :pitch-counter 
                       (filter 
                         #(and (#{ :dash :pitch } (:my_type %))
                               (:pitch-counter %))
                         (:items beat)))
        subdivisions (:subdivisions beat)
        first-pitch (some #(if (= :pitch (:my_type %)) %) (:items beat))
        ;;_ (println first-pitch)
                      ;; special case S-R. Maybe applies whenever fraction
        ;; is over 1/2 
        ;;_ (println subdivisions)
        ;;_ (println (:fraction_array first-pitch))
        ;; _ (println "frac" (/ (:numerator first-pitch) (:denominator first-pitch)))

        do-not-beam-case (and (#{3 5 7 9} subdivisions)  ;; TODO: test
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
                           ;;   (:fraction_array first-pitch)))
        ;; _ (println "do-not-beam-case" do-not-beam-case)
        beamable? (and (> (count pitch-ids) 1)
                       (not do-not-beam-case))
       ;; _ (println beamable? "beamable")
        ;;
        beat-ary (map (fn draw-beat-item[ item]
                        (let [
                              ;; If this is the first pitch or tied rest and there are
                              ;; more than one in the beat, add [ to end of note to
                              ;; indicate beam start.
                              beam-start-or-end    (cond (and beamable?
                                                              (= (first pitch-ids) (:pitch-counter item)))
                                                         lilypond-beam-start 
                                                         (and beamable?      
                                                              (= (last pitch-ids) (:pitch-counter item)))
                                                         lilypond-beam-end)
                              my-type (:my_type item)]
                          (cond (#{:pitch :dash} my-type)
                                (draw-pitch item false (:subdivisions beat)
                                            beam-start-or-end)
                                true
                                (str "<item" item " Unknown my-type" my-type ">"))))
                      (:items beat))
        beat-content (join " " beat-ary)
        ]
    (if (and (not (#{0 1 2 4 8 16 32 64 128} (:subdivisions beat)))
             (not (beat-is-all-dashes? beat)))
      ;; example: SRGRGmGmp  emits 2/9 followed by 9 16th notes. 2/9 * 9/16 = 1/8 which seems wrong. should be 4/9 * 9/16= 1/4
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

(defn merge-tied-notes[measure]
  measure)

(defn zmerge-tied-notes[measure]
  (let [x 1
        combined-beats (atom [])
        measure2 (assoc measure :items  
                        (reverse (map (fn[my-beat]
                                        ;;(pprint my-beat)
                                        (if (and 
                                              (= 1 (:subdivisions my-beat))
                                              (:dash_to_tie (first (:items my-beat))) 
                                              )
                                          (do 
                                            ;;(println "zzz")
                                            (swap! combined-beats (fn[x1] (conj x1 my-beat)))
                                            (assoc my-beat :marked-for-removal true)
                                            )
                                          my-beat 
                                          ))
                                      (:items measure) 
                                      )))
        ]
    ;;(println "****")
    ;;(pprint @combined-beats)
    ;; (println "****")
    measure2
    )
  )

(defn draw-measure[measure]
  ;; It would be nice to combine those pitches which are tied and take up one 
  ;; beat!!! TODO
  (:pre (= :measure (:my_type measure))) ;; TODO: not checking it!!!
  (str 
    (render partial-template 
            {:beat-count (:beat_count measure)})

    " "
    (join " "
          (map 
            (fn draw-measure-item[item]
              (let [my-type (:my_type item)]
                (cond 
                  (= :beat my-type)
                  (draw-beat item)
                  true
                  (str "<unknown my-type" my-type ">")
                  )))
            (:items measure)
            )))) 

(def draw-line-break
  ;; Lilypond text for line break. TODO: review emitting of line breaks
  ;; and repeat symbols. I believe that a repeat symbol followed by break
  ;; causes lilypond not to print the right repeat 
  "\\break\n")

(defn draw-line[line & options]
  "line consists of optional :line_number followed by :barline :measure :measure etc "
  "We don't render line numbers in lilypond. (yet)"
  "TODO: use the atoms properly"
  (let [in-slur (atom false)
        in-times (atom false)
        at-beginning-of-first-measure-of-line (atom false)
        first-item (first (:items line))
        my-items (if (= :line_number (:my_type first-item))
                   (rest (:items line))
                   (:items line))

        ] 
    (str
      (join " " 
            (map (fn draw-line-item[item]
                   (let [my-type (:my_type item)]
                     (cond (= :measure my-type)
                           (draw-measure (merge-tied-notes item))
                           (:is_barline item)
                           (draw-barline item)
                           true
                           (str "<??" my-type "??>"))))
                 my-items)) 
      " " 
      ;; TODO: only draw line break if not right repeat at end of line???
      draw-line-break) 
    ))


(defn has-after-ornament[pitch]
  ;; (pprint pitch)
  (if (not= :pitch (:my_type pitch))
    false
    ;; else
    (= :after (:placement (get-ornament pitch)))  
    ))
;;(pprint pitch-with-after-ornament)
;;(pprint (has-after-ornament pitch-with-after-ornament))
;;      console.log "line-to-lilypond" if false
;;      line-to-lilypond-array(line,options).join ' '
;;    
;;    has-after-ornament = (pitch) ->
;;      return false if !pitch?
;;      ornament=get-ornament(pitch)
;;      return false if !ornament?
;;      ornament?.placement is "after"
;;    
;;    line-to-lilypond-array = (line,options={}) ->
;;      # Line is a line from the parsed doremi-script


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

(defn lilypond-transpose[composition-data]
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
              {:key (normalized-pitch->lilypond-pitch (upper-case (:key composition-data)))}))))

(defn extract-lyrics[x]
  (map :syllable (filter 
                   #(and (not (:pointer %)) (#{:pitch :dash} (:my_type %))
                         (:syllable %))
                   (my-seq2 x))))


(defn to-lilypond[doremi-data]
  "TODO: render lyrics??"
  :pre (map? doremi-data)
  :pre (= :composition (:my_type doremi-data))
  "Takes parsed doremi-script and returns lilypond text"
  ""
  (let [ src "source here"
        template (-> "lilypond/composition.tpl" resource slurp trim)
        key-template (-> "lilypond/key.tpl" resource slurp trim)
        ]
    (render template 
            {:transpose-snip (lilypond-transpose doremi-data) 
             :extracted-lyrics (apply str (join " " (extract-lyrics doremi-data)))
             :beats-per-minute 200
             :title (:title doremi-data)
             :author (:author doremi-data)
             :src-snippet (str  "%{\n " (lilypond-escape (:source doremi-data)) " \n %}\n")
             :time-signature-snippet (if (:time_signature doremi-data)
                                       (render time-signature-template 
                                               {:time-signature (:time_signature doremi-data)})
                                       ;; else
                                       omit-time-signature-snippet) 

             :key-snippet (render key-template { :key "c"  ;; Always set key to c !! Transpose is used to move it to the right key
                                                :mode (lower-case (:mode doremi-data "major"))
                                                }) 
             :notes (join "\n" (map draw-line (:lines doremi-data))) 
             })))



;;;;;;;;;;;; For testing ;;;;;
(comment
 ;; (use 'clojure.stacktrace) 
 ;; (print-stack-trace *e)
  (println
    (to-lilypond 
      (doremi_script_clojure.core/doremi-script-text->parsed-doremi-script 
        (-> "fixtures/all_tuples.txt" resource slurp)
        ;;   "-- S - R"
        ))

    ))

(comment
  (println (to-lilypond 

             (doremi_script_clojure.core/doremi-script-text->parsed-doremi-script 
               ;;"S------------R--"
               ;;   "S----R--"
               ;; "S-GG m-P-"
               "S-R"
               )))
  )

(comment (pprint
           (doremi_script_clojure.core/doremi-script-text->parsed-doremi-script 
             ;;"S------------R--"
             ;;   "S----R--"
             "S - -"
             )))
(comment
  (println (to-lilypond 

             (doremi_script_clojure.core/doremi-script-text->parsed-doremi-script 
               ;;"S------------R--"
               ;;   "S----R--"
               "g - -"
               )))
  )
(comment
  (pprint (doremi_script_clojure.core/doremi-text->parse-tree "P\n m"))
  (pprint (doremi_script_clojure.core/doremi-script-text->parsed-doremi-script " n\nP d"))
  (println
    (to-lilypond (doremi_script_clojure.core/doremi-script-text->parsed-doremi-script 
                   ;;"S------------R--"
                   ;;   "S----R--"
          "<P d>"
          ;;         "P\n m"
                   )))
  )
(comment
  (println 
    (to-lilypond (doremi_script_clojure.core/doremi-script-text->parsed-doremi-script 
  ;;             "SRGm -PDnDPmG"
    ;;   (-> "fixtures/bansuriv3.txt" resource slurp)
               ;;"S------------R--"
               ;;   "S----R--"
               ;; "S-GG m-P-"
             ;;  "S-R SRGm S---R SRGm  S----R--- S----R--"
  ;;        "<P d>"
                   
"<S> <S - >  <   S    R   G >  < - S >"
               )))
 )
 ;; (print-stack-trace *e)

