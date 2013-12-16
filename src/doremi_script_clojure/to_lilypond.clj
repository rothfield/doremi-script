(ns doremi_script_clojure.to_lilypond
  (:require	
    [clabango.parser :refer [render]]
    [clojure.java.io :refer [resource]]
    [clojure.string :refer [join upper-case lower-case]] 
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
  "includes dash (-) -> r "
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
  (-> "lilypond/grace_note_pitch.tpl" resource slurp))

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
;;      ornament=get-ornament(pitch)
;;      grace1=grace2=grace-notes=""
;;      if ornament?.placement is "after"
;;        if pitch.tied?
;;          suppress-slurs=true
;;        if context.in-slur
;;          suppress-slurs=true
;;        if !context.in-slur #TODO: unfunkify
;;          begin-slur="("
;;        grace1 = "\\afterGrace "
;;        #grace2="( { #{lilypond-grace-notes(ornament)}) }"
;;        grace2=" { #{lilypond-grace-notes(ornament,suppress-slurs)} }"
;;      if ornament?.placement is "before"
;;      #  \acciaccatura { e16 d16 } c4
;;        suppress-slurs=true # FOR NOW
;;        grace1= "\\acciaccatura {#{lilypond-grace-notes(ornament,suppress-slurs)}}"
;;      extra-end-slur=""
;;      if special-case
;;        extra-end-slur=")"
;;      # Don't use tie if ornament is after!
;;      if (ornament?.placement is "after") and pitch.tied?
;;        console.log "OMG" if false
;;        lilypond-symbol-for-tie=""
;;      "#{grace1}#{lilypond-pitch}#{lilypond-octave}#{duration}#{lilypond-symbol-for-tie}#{mordent}#{begin-slur}#{extra-end-slur}#{end-slur}#{ending}#{chord}#{grace2}"
;;    

;;      # From lilypond docs:
;;      # If you want to end a note with a grace, 
;;      # use the \afterGrace command. It takes two 
;;      # arguments: the main note, and the 
;;      # grace notes following the main note.
;;      #
;;      #  c1 \afterGrace d1( { c32[ d]) } c1
;;      #
;;      #  Use
;;      #  \acciaccatura { e16 d16 } c4
;;      #  for ornaments with ornament.placement is "before"
;;    
;;    
;;      # The afterGrace in lilypond require parens to get lilypond
;;      # to render a slur.
;;      # The acciatura in lilypond don't require parens to get lilypond
;;      # to render a slur.
;;      #
;;    
;;      EXAMPLES = '''
;;    \partial 4*2  | \afterGrace c'4( { b32[ d'32 c'32 b32 c'32] } c'8) d'8 \break
;;    \partial 4*2  | \afterGrace c'4~ { b32[ d'32 c'32 b32 c'32] } c'8 d'8 \break
;;     \partial 4*2  | c'4~ c'8 d'8 \break
;;     '''
;;      #
;;      ornament=get-ornament(pitch)
;;      grace1=grace2=grace-notes=""
;;      if ornament?.placement is "after"
;;        if pitch.tied?
;;          suppress-slurs=true
;;        if context.in-slur
;;          suppress-slurs=true
;;        if !context.in-slur #TODO: unfunkify
;;          begin-slur="("
;;        grace1 = "\\afterGrace "
;;        #grace2="( { #{lilypond-grace-notes(ornament)}) }"
;;        grace2=" { #{lilypond-grace-notes(ornament,suppress-slurs)} }"
;;      if ornament?.placement is "before"
;;      #  \acciaccatura { e16 d16 } c4
;;        suppress-slurs=true # FOR NOW
;;        grace1= "\\acciaccatura {#{lilypond-grace-notes(ornament,suppress-slurs)}}"
;;      extra-end-slur=""
;;      if special-case
;;        extra-end-slur=")"
;;      # Don't use tie if ornament is after!
;;      if (ornament?.placement is "after") and pitch.tied?
;;        console.log "OMG" if false
;;        lilypond-symbol-for-tie=""
;;      "#{grace1}#{lilypond-pitch}#{lilypond-octave}#{duration}#{lilypond-symbol-for-tie}#{mordent}#{begin-slur}#{extra-end-slur}#{end-slur}#{ending}#{chord}#{grace2}"
;;    
;;    


(def fraction-to-lilypond
  ;;       # Todo: use fractions.js
  ;;       # TODO: have to tie notes for things like 5/8
  ;;       # which would be an 1/8th and a 32nd
  ;;       # To do it right should perhaps use fractional math as follows:
  ;;       # 5/8 = 1/2 + 1/8 => 1/8 + 1/32
  {

   "2/1" "2"
   "3/1" "2."
   "4/1" "1"
   "5/1" "1.."
   "1/1" "4"
   "1/2" "8"
   "1/3" "8"  ;; 1/3 1/5 1/7 all 8th notes so one beat will beam together
   "1/9" "8"
   "1/11" "8"
   "1/13" "8"
   "1/5" "16"
   "2/5" "8"
   "3/5" "8." ;;TODO should be tied
   "4/5" "4" ;;TODO should be tied
   "5/5"4
   "6/6"4
   "7/7"4
   "9/9"4
   "10/10"4
   "11/11"4
   "12/12"4
   "13/13"4
   "1/7" "32" ;; ??? correct???hhhhhhhhhh
   "2/7" "16" ;; ??? correct???hhhhhhhhhh
   "3/7" "16." ;; ??? correct???hhhhhhhhhh
   "4/7" "8" ;; ??? correct???hhhhhhhhhh
   "5/7" "8.." ;; ??? correct???hhhhhhhhhh
   "6/7" "8.." ;; ??? correct???hhhhhhhhhh
   "6/8" "8." 
   "2/3" "4"
   "2/8" "16"
   "3/8" "16."  ;; 1/4 + 1/8
   "5/8" "8"   ;; TODO: WRONG
   "4/8" "8"
   "7/8" "8.." ;; 1/2 + 1/4 + 1/8
   "1/6" "16"
   "2/6" "8"
   "3/6" "4"
   "4/6" "4" ;; NOT SURE ????
   "5/6" "8.." ;;  WRONGnot sure TODO??
   "2/2" "4"
   "3/3" "4"
   "4/4" "4"
   "8/8" "4"
   "1/4" "16"
   "2/4" "8"
   "3/4" "8."
   "4/16" "16"
   "3/16" ""
   "1/8" "32"
   })



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

(defn calculate-lilypond-duration[my-numerator my-denominator]
  ;;  (:pre (not (nil? my-numerator)))
  ;; (:pre (not (nil? my-denominator)))
  ;;(println "entering calculate-lilypond-duration, my-numerator my-denominator " my-numerator my-denominator )
  "TODO: rewrite"
  (if (= my-numerator my-denominator)
    "4"
    ;; else
    (let [frac (str my-numerator "/" my-denominator)
          ;; _ (println "frac is ")
          ;; _ (println frac)
          looked-up-duration (fraction-to-lilypond frac)
          ;;_ (println looked-up-duration)
          ]
      (if looked-up-duration
        looked-up-duration
        ;; else
        "16"
        ))))

(defn lilypond-escape[x]
  ;;(println "x is " x)
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
         :reverse-final-barline "\\bar \"|.\""
         :final-barline "\\bar \"||\" "
         :double-barline "\\bar \"||\" " 
         :single-barline "\\bar \"|\"" 
         :left-repeat "\\bar \"|:\"" 
         :right-repeat "\\bar \":|\"" 
         }
        ]
    (or (barline-type my-map) (:single-barline my-map))
    ))

(def partial-template
  (-> "lilypond/partial.tpl" resource slurp))
(def ending-template
  (-> "lilypond/ending.tpl" resource slurp))



(defn normalized-pitch-to-lilypond[pitch in-slur]
  "Also handles dashes"
  "Render a pitch/dash as lilypond"
  (let
    [ 
     ;;if pitch.dash_to_tie and has_after_ornament(context.last_pitch)
     ;; _ (println "normalized-pitch-to-lilypond, pitch=") 
     ;; _ (pprint pitch)
     ornament (get-ornament pitch)
     placement (:placement ornament)
     has-after-ornament (= :after placement)
     has-before-ornament (= :before placement)    
     suppress-slurs (and has-after-ornament
                         (or in-slur (:tied pitch)))
     after-ornament-start (if has-after-ornament
                            "\\afterGrace ")
     before-ornament-snippet (if has-before-ornament
                               (str "\\acciaccatura {" (lilypond-grace-notes ornament suppress-slurs) "}"))

     after-ornament-end (if has-after-ornament
                          (str " { " (lilypond-grace-notes ornament suppress-slurs) " }")) 

     begin-slur (if (or (get-attribute pitch :begin_slur)
                        (and (not in-slur) has-after-ornament))
                  "(" )
     extra_end_slur  (if (and (:dash_to_tie pitch) 
                              has-after-ornament)
                       ")")
     ;;begin-slur (if (and (= :after placement)
     pitch-template (-> "lilypond/pitch.tpl" resource slurp)
     rest-template (-> "lilypond/rest.tpl" resource slurp)
     lilypond-octave  (octave-number->lilypond-octave (:octave pitch))
     duration (calculate-lilypond-duration 
                (:numerator pitch) 
                (:denominator pitch))
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
     ]
    ;; this function returns the value of the following cond:
    (cond (:ignore pitch2)
          ""
          (:pointer pitch)
          ""
          (and (= :dash (:my_type pitch2)) 
               (not (:dash_to_tie pitch2)))
          (render rest-template {
                                 :duration duration
                                 :chord chord
                                 :ending ending-snippet                
                                 })
          (or (= :pitch (:my_type pitch2))
              (= :dash (:my_type pitch2)))
          ;;
          ;;{{before-ornament-snippet}}{{after-ornament-start}}{{lilypond-pitch}}{{lilypond-octave}}{{duration}}{{lilypond-symbol-for-tie}}{{mordent}}{{begin-slur}}{{extra-end-slur}}{{end-slur}}{{ending}}{{chord}}{{after-ornament-end}}
          (render pitch-template 
                  {
                   :before-ornament-snippet before-ornament-snippet
                   :after-ornament-start after-ornament-start
                   :lilypond-pitch 
                   (normalized-pitch->lilypond-pitch (:normalized_pitch pitch2))
                   :lilypond-octave  (octave-number->lilypond-octave (:octave pitch2))
                   :duration duration
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
                   :after-ornament-end after-ornament-end
                   }
                  ))))

(def lilypond-break  "\\break\n")

(defn line-to-lilypond-array[line & options]
  ;; Try processing recursively?
  ;; 
  ;;(println "entering line-to-lilypond-array")
  ;;(pprint line)
  ;;    # Line is a line from the parsed doremi-script
  ;;    # Returns an array of items - join them with a string
  (let [in-slur (atom false)
        in-times (atom false)
        at-beginning-of-first-measure-of-line (atom false)
        process-measure (fn process-measure[measure] 
                          (render partial-template 
                                  {:beat-count (:beat_count measure)})
                          )
        process-beat (fn process-beat[beat] 
                       ;; TODO
                       "")
        process-barline (fn process-barline[x] 
                          (barline->lilypond-barline (:my_type x)))
        process-beat (fn process-beat[x] 
                       ""
                       )
        process-item (fn process-item[item]
                       (let [ my-type (:my_type item) ]
                         ;;(println "in process-item, my-type is " my-type)
                         (cond 
                           (or (= :pitch my-type) 
                               (= :dash my-type))

                           ;;        last-pitch=item  #use this to help render ties better(hopefully)
                           ;;        if dashes-at-beginning-of-line-array.length > 0
                           ;;          for dash in dashes-at-beginning-of-line-array
                           ;;            ary.push normalized-pitch-to-lilypond(dash,{last-pitch:last-pitch})
                           ;;          dashes-at-beginning-of-line-array=[]
                           ;;        ary.push normalized-pitch-to-lilypond(item,{in-slur:  in-slur,last-pitch:last-pitch})
                           (normalized-pitch-to-lilypond item in-slur)
                           (= :measure my-type)
                           (process-measure item)
                           (= :beat my-type)
                           (process-beat item)
                           (:is_barline item)
                           (process-barline item)
                           true
                           ""
                           )))
        ]
    (conj (map process-item (filter :my_type (my-seq2(:items line)))) lilypond-break)
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

(defn beat-is-all-dashes[beat] 
  (:pre (= (:my_type "beat")))
  (not-any? #(= :pitch (:my_type %)) (:items beat)))

(defn is-abc-line[line]
  (if (nil? (:kind line))
    false
    ;; else
    (re-find #"abc" (:kind line))))

(defn notation-is-in-abc[composition-data]
  (some is-abc-line (:lines composition-data)))

(def transpose-template
  (-> "lilypond/transpose.tpl" resource slurp))
(def time-signature-template
  (-> "lilypond/time_signature.tpl" resource slurp))

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



(defn line-to-lilypond[line & options]
  ;;(println "my-seq2 line is")
  ;;(pprint (map :my_type (filter :my_type (my-seq2 line))))
  (join " " (line-to-lilypond-array line)))
;;(to-lilypond "" {})



(defn to-lilypond[doremi-data]
  :pre (map? doremi-data)
  :pre (= :composition (:my_type doremi-data))
  "Takes parsed doremi-script and returns lilypond text"
  ""
  (let [ src "source here"
        template (-> "lilypond/composition.tpl" resource slurp)
        key-template (-> "lilypond/key.tpl" resource slurp)
        ]
    ;;(println "doremi-data:")
    ;;(pprint doremi-data)
    ;;(println "time signature is: " (:time_signature doremi-data))
    (render template 
            {:transpose-snip (lilypond-transpose doremi-data) 
             :extracted-lyrics (apply str (join " " (extract-lyrics doremi-data)))
             ;; :zzz (pprint (extract-lyrics doremi-data))
             :beats-per-minute 200
             :title-snippet ""
             :src-snippet (str  "%{\n " (lilypond-escape (:source doremi-data)) " \n %}\n")
             :time-signature-snippet (if (:time_signature doremi-data)
                             (render time-signature-template {:time-signature (:time_signature doremi-data) }))
             
             :key-snippet (render key-template { :key "c"  ;; Always set to c as we are transposing. TODO: review.
                                                :mode (lower-case (:mode doremi-data "major"))
                                                }) 
             :notes (join "\n" (map line-to-lilypond (:lines doremi-data))) 
             ;;:time-signature "4/4"
             })))

(comment
  (use 'clojure.stacktrace) 
  (print-stack-trace *e)
  (println
    (to-lilypond 
      (doremi_script_clojure.core/doremi-script-text->parsed-doremi-script 
               (-> "fixtures/waltz.txt" resource slurp)
     ;;   "-- S - R"
        ))

    ))


(comment
             (pprint (doremi_script_clojure.core/doremi-script-text->parsed-doremi-script 
               (-> "fixtures/waltz.txt" resource slurp)))



  )
(comment
  (println (to-lilypond 
             (doremi_script_clojure.core/doremi-script-text->parsed-doremi-script 
               ;"S--g - R | S R G m |"
               ;;        "S--g -"
               (-> "fixtures/waltz.txt" resource slurp)
               )))
  )
;;)
