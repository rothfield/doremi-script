(ns doremi-script.to-lilypond
    #?(:cljs
(:require-macros [clojure.core.strint :as strint])
)
  (:require	
    [clojure.string :refer 
     [replace-first lower-case join] :as string] 
    #?(:clj
    [clojure.core.strint :as strint]
    )
    [doremi-script.utils :refer [get-attributes
                                 items map-even-items is? get-attribute]]
    ))

;;(defn v[] 30.5)
;;(println (<< "This trial required ~{v}ml of solution."))

;;; TODO: try making it cljc and add to-lilypond functionality to the javascript pp

;;;; *********************READ ME*****************
;;;; What works and doesn't work:
;;;; This seems to work well:
;;;; melody =  {
;;;;        \accidentalStyle modern-cautionary
;;;;        \once \override Staff.TimeSignature #'stencil = ##f
;;;;        \key c \major
;;;;        \autoBeamOn  
;;;;        \override Staff.TimeSignature #'style = #'()
;;; TODO: add back in \partial 4*1 measures !!!!! Old version worked better
;;; see  http://ragapedia.com/open/Bansuri_V2_Traditional_1327249816068.ly          
;;;
;;; WHAT DOESN'T WORK: 
;;; cadenzaOn
;;;
;;;



(comment
  ;; to use in repl:
  ;; cpr runs current file in vim
  ;; cp% runs current form. vim-fireplace
  (set! *warn-on-reflection* true)
  (use 'doremi-script.utils :reload) (ns doremi-script.to-lilypond) 
  (use 'doremi-script.to-lilypond :reload) (ns doremi-script.to-lilypond) 
  (use 'clojure.stacktrace) 
  (print-stack-trace *e)
  (use 'doremi-script.test-helper :reload)  ;; to reload the grammar
  (print-stack-trace *e)
  (pst)
  )


(def debug false)

(def normalized-pitch->lilypond-pitch
  ;; includes dash (-) -> r "
  {
   "-" "r", "C" "c", "C#" "cs", "Cb" "cf", "Db" "df", "D" "d", "D#" "ds",
   "Eb" "ef", "E" "e", "E#" "es", "F" "f", "Fb" "ff", "F#" "fs", "Gb" "gf",
   "G" "g", "G#" "gs", "Ab" "af", "A" "a", "A#" "as", "Bb" "bf", "B" "b",
   "B#" "bs", })

(defn lilypond-escape[x]
  ;; (println "x is " x)
  ;;      # Anything that is enclosed in %{ and %} is ignored  by lilypond
  (when x (string/replace x #"\{%" "% {"))
  ) 

(def omit-time-signature-snippet
  "\\once \\override Staff.TimeSignature #'stencil = ##f"
  )
(defn  is-abc-composition[x] 
  (assert (map? x))
  (= :abc-composition (get x :kind)))

(def valid-lilypond-mode?
  #{:ionian :dorian :phrygian :lydian :mixolydian :aeolian :locrian
    :minor :major })
(defn key-signature-snippet[attributes]
  (let [my-mode (->
                  (get attributes :mode "major")
                  lower-case
                  keyword)
        _ (println "my-mode is" my-mode)
        my-mode2 (if (not (valid-lilypond-mode? my-mode))
                   :major
                   my-mode)
        _ (println "my-mode2 is" my-mode2)
        ]
    (assert (map? attributes))
    (str "\\" "key "
         (if (is-abc-composition attributes)
           (str (normalized-pitch->lilypond-pitch (:key  attributes
                                                        "C"))
                " "
                (str "\\" (name my-mode2) "\n"))
           ;; else
           (str "c " 
                "\\" (name my-mode2)
                "\n"
                ) 
           ))))

(defn time-signature-snippet[x]
  (if x
    (str "\\time " x)
    omit-time-signature-snippet))






(def pitch->lilypond-pitch
  ;; includes dash (-) -> r "
  {
   "-" "r", "S" "c", "S#" "cs", "Sb" "cf", "r" "df", "R" "d", "R#" "ds",
   "g" "ef", "G" "e", "G#" "es", "m" "f", "mb" "ff", "M" "fs", "Pb" "gf",
   "P" "g", "P#" "gs", "d" "af", "D" "a", "D#" "as", "n" "bf", "N" "b",
   "N#" "bs", })


(defn beat->beat-divisions[beat]
  {:pre [(is? :beat beat)]
   :post [ (integer? %)]
   }
  (->> beat (filter vector?) (map first) (filter #{:pitch :dash}) count) 
  )


(defn start-beat[accum beat]
  (assoc accum 
         :divisions (beat->beat-divisions beat)
         :beat-pitches []
         ))

(defn barline->lilypond-barline[
                                [_ [barline-type] ] ;; destructuring fun
                                ]
  " maps barline-type field for barlines"
  (let [my-map
        {
         :reverse-final-barline "\\bar \".|\""
         :final-barline "\\bar \"|.\" "
         :double-barline "\\bar \"||\" " 
         :single-barline "\\bar \"|\"" 
         :left-repeat 
         "\\bar \".|:\"" 
         :right-repeat "\\bar \":|.\"" 
         } ]
    (str (get my-map barline-type (:single-barline my-map)) " ")
    ))

(defn octave-number->lilypond-octave[num]
  (let [tick "'"
        comma ","]
    ;; Middle c is c'
    (cond (nil? num)
          tick
          (>= num 0)
          (apply str (take (inc num) (repeat tick)))
          true
          (apply str (take (dec (- num)) (repeat comma))))))

(defn start-pitch[accum pitch]
  (-> accum (assoc :state :collecting-pitch-in-beat
                   :current-pitch  { :obj pitch  :micro-beats 1}
                   )) 
  )

(defn start-line[accum obj]
  accum
  )

(defn save-barline[accum barline]
  (assoc accum :last-barline
         (barline->lilypond-barline barline)
         )
  )

(defn lilypond-headers[accum composition]
  accum)

(defn ratio->lilypond-durations
  "ratio->lilypond-durations(3 4) => ['8.']   Ratio is ratio of 1/4 note "
  [my-numerator subdivisions-in-beat]
  { :pre [ (integer? my-numerator)
          (integer? subdivisions-in-beat)]
   :post [ ;; (do (println %) true)
          (vector? %)] 
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
    (if (integer? my-ratio)
      ({ 1 ["4"]
        2 ["2"]
        3 ["2."]
        4 ["1"]  ;; review
        8 ["1" "1"]
        } my-ratio
       (into [] (repeat my-numerator "4"))
       )
      ;; else - it is a fraction
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
             (into [] (take my-numerator (cycle (str new-denominator))))
             ) 
        ))))

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


;; It might be better if the pitches hadn't already
;; been turned to lilypond strings.
;;
(defn beam-beat-pitches[str-array]
  {
   :pre [(vector str-array)]
   :post [(vector? %)]
   }
  "beam a beat. Don't beam if beat contains rests."
  "Insert [ and ] after the duration. Use regex to do replacement"
  (let [last-idx (dec (count str-array))]
    (if (or (= 1 (count str-array))
            (some  (fn[^java.lang.String x] (.startsWith x "r")) str-array))
      str-array
      ;; else
      (do
        ;;(println "beam them")
        (assoc str-array
               0 
               (replace-first
                 (first str-array)
                 #"(\d+\.*)" "$1[") 

               last-idx
               (replace-first
                 (last str-array)
                 #"(\d+\.*)" "$1]") 
               )
        ))))

(defn enclose-beat-in-times[subdivisions beat-str]
  {
   :pre [(string? beat-str)
         (integer? subdivisions)]
   :post [(string? %)]
   }
  (if (not (#{0 1 2 4 8 16 32 64 128} subdivisions))
    (str "\\times "
         (tuplet-numerator-for-odd-subdivisions 
           subdivisions)
         "/" 
         subdivisions
         "{ "
         beat-str " } ")
    beat-str))
(def obj  [:measure [:beat [:pitch "C"]]])

(defn finish-measure[accum]
  (println "in finish-measure, accum=" accum)
  accum
  )
;;  (-> accum (assoc :current-pitch nil :last-barline nil)
;;     (update-in [:output] #(str % " " 
;;                               "\\partiallast-barline


(defn start-measure[accum measure]
  (println "in start-measure, measure =")
  (println measure)
  (let [
        measure-beat-count (->> measure 
                                rest 
                                (filter #(= :beat (first %)))
                                count
                                ) 
        last-barline1 (or (get accum :last-barline)
                          "\\bar \"|\"")
        last-barline2 (if (:in-notes-line-beginning accum)
                        (when debug (println ":in-notes-line-beginning, omitting barline")
                          nil)
                        last-barline1) 
        last-barline (get accum :last-barline)
        ]
    (-> accum (assoc :current-pitch nil :last-barline nil)
        (update-in [:output] #(str % " " last-barline
                                   " "
                                   (str "\\partial 4*" measure-beat-count)
                                   ))
        )))

(defn finish-beat[accum]
  ;; see also autoBeam... in printHeaders
  (assoc accum
         :output (str  (:output accum) " " 
                      (->> accum :beat-pitches beam-beat-pitches (join " ") (enclose-beat-in-times (:divisions accum))))
         :beat-pitches []
         :divisions 0))

(defn chord-snippet[obj]
  { :pre [ (vector? obj)   ] }
  (let [
        chord (last (first (filter #(and (vector? %) (= :chord (first %))) obj)))
        ]
    (when chord (str "^\"" chord "\""))
    ))


(defn finish-dashes[accum]
  (let [dash (:current-dash accum)
        divisions (accum :divisions)
        micro-beats (get-in accum [:dash-microbeats])
        ary (ratio->lilypond-durations micro-beats divisions)
        first-duration (first ary)
        first-rest (str "r" first-duration (chord-snippet dash))
        rests (str first-rest (join " " (map (partial str "r") (rest ary))))
        ]
    (update-in accum [:beat-pitches] conj rests )
    ))


(defn start-dash[accum dash]
  (assoc accum :state :collecting-dashes-in-beat
         :dash-microbeats 1
         :current-dash dash
         ))


(defn get-ending[obj]
  (last (get-attribute obj :ending)))

(defn get-syl[pitch]
  (last (get-attribute pitch :syl)))

(defn ending-snippet[obj]
  (when-let [ending (get-ending obj)]
    (str "^\"" ending "\"")
    ))



(defn normalized-pitch->kommal[x]
  (get {"D" "Db"
        "E" "Eb"
        "A" "Ab"
        "B" "Bb"} x x))

(defn pitch-and-octave[pitch]
  (let [pitch2 
        (if (get-attribute pitch :kommal-indicator)
          (normalized-pitch->kommal (second pitch))
          (second pitch))]
    (str  (normalized-pitch->lilypond-pitch pitch2)
         (-> pitch (get-attribute :octave) second octave-number->lilypond-octave))
    ))


(defn get-ornament-pitches[ornament]
  (-> ornament rest drop-last)
  )

(defn grace-note-pitches[ornament]
  ;; returns a string of grace note pitches.
  ;; Use 16 as duration and beam them if there is more than one.
  ;; Beamed notes look like this e'16[ d'16]
  (let [pitches (get-ornament-pitches ornament)
        my-count (count pitches)
        items (into [] (map (fn[x] (str (pitch-and-octave x) "16")) pitches))
        ;; Update 1st and last items to add beams
        items2 (if (> my-count 1)
                 (assoc items 0 (str (get items 0) "[")
                        (dec my-count) (str (last items) "]"))
                 items)
        ]
    (join " " items2)))

(defn finish-pitch[accum]
  (when debug (println "finish-pitch"))
  (when debug (println (remove :output accum)))
  (let [pitch (:obj (:current-pitch accum))
        syl  (get-syl pitch)

        begin-slur (if (some #(and (vector? %) (= :begin-slur (first %)))
                             pitch)
                     "(") 
        _ (when begin-slur
            (reset! (:in-slur accum) true))
        end-slur (if (some #(and (vector? %) (= :end-slur (first %)))
                           pitch)
                   ")") 
        _ (when end-slur
            (reset! (:in-slur accum) false))
        my-pitch-and-octave  (pitch-and-octave pitch)
        divisions (accum :divisions)
        after-ornament  (->> pitch (filter #(is? :ornament %)) (filter #(= :after (last %))) first)
        before-ornament  (->> pitch (filter #(is? :ornament %)) (filter #(= :before (last %))) first)
        micro-beats (get-in accum [:current-pitch :micro-beats])
        durations (ratio->lilypond-durations micro-beats divisions)
        first-pitch  
        (str 
          (when before-ornament
            (str "\\grace {" (grace-note-pitches before-ornament) "}"))
          (when after-ornament "\\afterGrace ")
          my-pitch-and-octave
          (first durations) 
          end-slur
          begin-slur
          (chord-snippet pitch)
          (ending-snippet pitch)
          (when (get-attribute pitch :mordent)
            "\\mordent") 
          (when (and after-ornament  (not @(:in-slur accum)))
            "(")
          (when after-ornament ;; ornament
            (str  "{" (grace-note-pitches after-ornament) 
                 (when (and after-ornament  (not @(:in-slur accum)))
                   ")")
                 "}"))
          ) ;; end str
        pitches  (if (= 1 (count durations))
                   first-pitch
                   (str first-pitch "~"
                        (join "~" (map (partial str my-pitch-and-octave) 
                                       (rest durations)))))
        ]
    (-> accum  (update-in [:beat-pitches] conj pitches) (update-in [:syllables] str syl " ")
        )
    ))

(defn finish-line[accum]
  ;;  (update-in accum [:output] str " \\break\n"))

  (-> accum (update-in [:output] str 
                       " " (accum :last-barline) 
                       ;;   "\\break" ;; controversial- 2015 if one score per line, omit break
                       ;;  " "
                       ;; "\\grace s16"  ;; fixes bug where repeats don't show https://lists.gnu.org/archive/html/lilypond-user/2013-01/msg00673.html
                       " \n")  (assoc :last-barline nil)))

(def TICK "'")
;; (transpose-snippet "Bb")
(defn transpose-snippet[my-key]
  { :pre [(or (string? my-key) (nil? my-key))]
   :post [(or (string? %) (nil? %))]
   }
  "Don't transpose if notation is in abc(future)"
  (cond 
    (nil? my-key) 
    ""
    (= "c" (lower-case my-key))
    ""
    true
    (str "\\transpose c' "
         (get normalized-pitch->lilypond-pitch  my-key "c'")
         (when (get #{"C" "D" "E" "F" "G"} (str (first my-key)))      
           TICK))
    ))

(defn lilypond-at-eof[accum]
  accum)


(defn lilypond-transition[accum obj]
  { :pre [
          (map? accum)
          (#{:lyrics-section :lyrics-line :stave :pitch :barline :measure :notes-line :line-number :composition
             :beat :dash :output :eof :attribute-section :source} (first obj))
          (:output accum)
          (:state accum)
          (keyword? (:state accum))
          (vector obj)]}
  (let [token (first obj)
        cur-state (:state accum)
        _ (when debug (println "Entering lilypond-transition\n" "state,token=" cur-state token))
        ]
    (case [cur-state token]
      [:start :composition]
      ;; accum ;;;(-> accum (lilypond-headers obj) 
      (-> accum (assoc :state :looking-for-attribute-section)) ;; REVIEW this line!!! TODO

      [:looking-for-source :source]
      (-> accum (assoc :state :looking-for-attribute-section))

      [:in-stave :notes-line]
      (-> accum (start-line obj) (assoc  :state :in-notes-line-beginning)) ;; review:lyrics (rest (last obj)))

      [:looking-for-attribute-section :lyrics-section]
      accum

      [:looking-for-attribute-section :stave]
      (-> accum (assoc :state :in-stave))

      [:looking-for-attribute-section :attribute-section]
      (assoc accum :state :looking-for-attribute-section) ;; support multiple attribute sections 

      [:in-notes-line-beginning :measure]
      (-> accum (start-measure obj) 
          (assoc :current-pitch nil :state :in-notes-line)) ;; maybe need to save current pitch

      [:in-notes-line :measure]
      (-> accum (start-measure obj))

      [:in-notes-line :beat]
      (-> accum (start-beat obj) (assoc :state :in-beat))

      [:in-notes-line-beginning :barline]
      ;;  (-> accum (save-barline obj) (assoc :state :in-notes-line))
      ;;  omit barline at beginning of line if it is a regular barline????
      (if (= :single-barline (first (second obj))) 
        (-> accum (assoc :state :in-notes-line))
        ;; else
        (-> accum (assoc :state :in-notes-line :last-barline (barline->lilypond-barline obj))))

      [:in-notes-line :barline]
      (-> accum (save-barline obj) (assoc :state :in-notes-line))

      [:in-notes-line :line-number]
      accum
      [:in-notes-line-beginning :line-number]
      (-> accum (assoc :state :in-notes-line-beginning))

      [:in-notes-line :stave]
      (-> accum finish-line (assoc :state :in-stave))

      [:looking-for-stave :eof]
      (-> accum lilypond-at-eof  (assoc :state :eof))
      [:looking-for-stave :attribute-section]
      accum
      [:looking-for-stave :stave]
      (-> accum (assoc :state :in-stave))

      [:in-notes-line :lyrics-line]
      (-> accum finish-line
          (assoc :state :looking-for-stave))

      [:collecting-pitch-in-beat :lyrics-section] ;; needed?
      (-> accum finish-pitch finish-beat finish-line
          (assoc :state :looking-for-stave))
      [:collecting-pitch-in-beat :lyrics-line] ;; needed?
      (-> accum finish-pitch finish-beat finish-line
          (assoc :state :looking-for-stave))
      [:collecting-pitch-in-beat :stave] ;; needed?
      (-> accum finish-pitch finish-beat finish-line
          (assoc :state :in-stave))

      [:looking-for-attribute-section :eof]
      (-> accum (assoc :output "No music entered" :state :eof))

      [:collecting-pitch-in-beat :eof]
      (-> accum finish-pitch finish-beat finish-line lilypond-at-eof)
      [:in-notes-line :lyrics-section]
      (-> accum finish-line (assoc :state :looking-for-stave))
      [:in-notes-line :eof]
      (-> accum finish-line lilypond-at-eof (assoc :state :eof))

      [:in-beat :eof]
      (-> accum finish-beat  (assoc :state :eof))

      [:in-beat :pitch]
      (start-pitch accum obj)

      [:in-beat :dash]  ;; dash at beginning of beat
      (start-dash accum obj)

      [:collecting-dashes-in-beat :lyrics-line]
      (-> accum finish-dashes finish-beat finish-line 
          (assoc :state :looking-for-stave))

      [:collecting-dashes-in-beat :lyrics-section]
      (-> accum finish-dashes finish-beat finish-line (assoc :state :looking-for-stave))
      [:collecting-dashes-in-beat :eof]
      (-> accum finish-dashes finish-beat lilypond-at-eof)

      [:collecting-dashes-in-beat :beat]
      (-> accum finish-dashes finish-beat (start-beat obj) (assoc :state :in-beat))

[:collecting-dashes-in-beat :dash]
(-> accum (update-in [:dash-microbeats] inc))

[:collecting-dashes-in-beat :pitch]
(-> accum finish-dashes (start-pitch obj) (assoc :state :collecting-pitch-in-beat)) 

[:collecting-dashes-in-beat :stave]
(-> accum finish-dashes finish-beat finish-line (assoc :state :in-stave)) 

[:collecting-dashes-in-beat :barline]
(-> accum finish-dashes finish-beat (save-barline obj) (assoc :state :in-notes-line))

[:collecting-pitch-in-beat :barline]
(-> accum finish-pitch finish-beat finish-measure (save-barline obj)
    (assoc :state :in-notes-line))

[:collecting-pitch-in-beat :pitch]
(-> accum finish-pitch (start-pitch obj)
    (assoc :state :collecting-pitch-in-beat))

[:collecting-pitch-in-beat :dash]
(-> accum (update-in [:current-pitch :micro-beats] inc))

[:collecting-pitch-in-beat :beat]
(-> accum finish-pitch finish-beat (start-beat obj) (assoc :state :in-beat))
;; tie previous note if new one is a dash!!!
)))

(defn stave-transition[accum obj]
  ;; state machine for a stave
  { :pre [ 
          (map? accum)
          (#{:lyrics-section :lyrics-line :stave :pitch :barline :measure :notes-line :line-number :composition
             :beat :dash :output :eof :attribute-section :source} (first obj))
          (:output accum)
          (:state accum)
          (keyword? (:state accum))
          (vector obj)]}
  (let [token (first obj)
        cur-state (:state accum)
        _ (when debug (println "Entering lilypond-transition\n" "state,token=" cur-state token))
        ]
    (case [cur-state token]
      [:start :composition]
      (-> accum (lilypond-headers obj) (assoc :state :looking-for-attribute-section))

      [:in-stave :notes-line]
      (-> accum (start-line obj) (assoc  :state :in-notes-line-beginning)) ;; review:lyrics (rest (last obj)))

      [:looking-for-attribute-section :lyrics-section]
      accum

      [:looking-for-attribute-section :stave]
      (-> accum (assoc :state :in-stave))

      [:looking-for-attribute-section :attribute-section]
      (assoc accum :state :looking-for-attribute-section) ;; support multiple attribute sections 

      [:in-notes-line-beginning :measure]
      (-> accum (start-measure obj) 
          (assoc :current-pitch nil :state :in-notes-line)) ;; maybe need to save current pitch

      [:in-notes-line :measure]
      (-> accum (start-measure obj))

      [:in-notes-line :beat]
      (-> accum (start-beat obj) (assoc :state :in-beat))

      [:in-notes-line-beginning :barline]
      ;;  (-> accum (save-barline obj) (assoc :state :in-notes-line))
      ;;  omit barline at beginning of line if it is a regular barline????
      (if (= :single-barline (first (second obj))) 
        (-> accum (assoc :state :in-notes-line))
        ;; else
        (-> accum (assoc :state :in-notes-line :last-barline (barline->lilypond-barline obj))))

      [:in-notes-line :barline]
      (-> accum (save-barline obj) (assoc :state :in-notes-line))

      [:in-notes-line :line-number]
      accum
      [:in-notes-line-beginning :line-number]
      (-> accum (assoc :state :in-notes-line-beginning))

      [:in-notes-line :stave]
      (-> accum finish-line (assoc :state :in-stave))

      [:looking-for-stave :eof]
      (-> accum lilypond-at-eof  (assoc :state :eof))
      [:looking-for-stave :attribute-section]
      accum
      [:looking-for-stave :stave]
      (-> accum (assoc :state :in-stave))

      [:in-notes-line :lyrics-line]
      (-> accum finish-line
          (assoc :state :looking-for-stave))
      [:collecting-pitch-in-beat :lyrics-section] ;; needed?
      (-> accum finish-pitch finish-beat finish-line
          (assoc :state :looking-for-stave))
      [:collecting-pitch-in-beat :lyrics-line] ;; needed?
      (-> accum finish-pitch finish-beat finish-line
          (assoc :state :looking-for-stave))
      [:collecting-pitch-in-beat :stave] ;; needed?
      (-> accum finish-pitch finish-beat finish-line
          (assoc :state :in-stave))
      [:looking-for-attribute-section :eof]
      (-> accum (assoc :output "No music entered" :state :eof))
      [:collecting-pitch-in-beat :eof]
      (-> accum finish-pitch finish-beat finish-line lilypond-at-eof)
      [:in-notes-line :lyrics-section]
      (-> accum finish-line (assoc :state :looking-for-stave))
      [:in-notes-line :eof]
      (-> accum finish-line lilypond-at-eof (assoc :state :eof))
      [:in-beat :eof]
      (-> accum finish-beat  (assoc :state :eof))
      [:in-beat :pitch]
      (start-pitch accum obj)
      [:in-beat :dash]  ;; dash at beginning of beat
      (start-dash accum obj)
      [:collecting-dashes-in-beat :lyrics-line]
      (-> accum finish-dashes finish-beat finish-line 
          (assoc :state :looking-for-stave))
      [:collecting-dashes-in-beat :lyrics-section]
      (-> accum finish-dashes finish-beat finish-line (assoc :state :looking-for-stave))
      [:collecting-dashes-in-beat :eof]
      (-> accum finish-dashes finish-beat lilypond-at-eof)
      [:collecting-dashes-in-beat :beat]
      (-> accum finish-dashes finish-beat (start-beat obj) (assoc :state :in-beat))
      [:collecting-dashes-in-beat :dash]
      (-> accum (update-in [:dash-microbeats] inc))

      [:collecting-dashes-in-beat :pitch]
      (-> accum finish-dashes (start-pitch obj) (assoc :state :collecting-pitch-in-beat)) 

      [:collecting-dashes-in-beat :stave]
      (-> accum finish-dashes finish-beat finish-line (assoc :state :in-stave)) 

      [:collecting-dashes-in-beat :barline]
      (-> accum finish-dashes finish-beat (save-barline obj) (assoc :state :in-notes-line))

      [:collecting-pitch-in-beat :barline]
      (-> accum finish-pitch finish-beat finish-measure (save-barline obj)
          (assoc :state :in-notes-line))

[:collecting-pitch-in-beat :pitch]
(-> accum finish-pitch (start-pitch obj)
    (assoc :state :collecting-pitch-in-beat))

[:collecting-pitch-in-beat :dash]
(-> accum (update-in [:current-pitch :micro-beats] inc))

[:collecting-pitch-in-beat :beat]
(-> accum finish-pitch finish-beat (start-beat obj) (assoc :state :in-beat))
;; tie previous note if new one is a dash!!!
)))



(defn staff-lyrics[stave]
  (assert (is? :stave stave))
  (clojure.string/join " " 
                       (->> stave items (filter #(is? :lyrics-line %)) first rest)))




(defn all-syls[composition]
  ;; For lilypond, where to get syllables? Try the syllables in the stave first
  (let [
        staves
        (filter #(is? :stave %) (rest composition))
        staves-syls (map
                      (fn[stave] (join " "  (mapcat rest (filter #(is? :syl %) (tree-seq vector? rest stave)))))
                      staves)
        syls-str (join "\n" staves-syls) 
        zzzsyls-str
        (join " "  (mapcat rest (filter #(is? :syl %) (tree-seq vector? rest composition))))
        lyrics-str 
        (join " "  (mapcat rest (filter #(is? :lyrics-line %) (tree-seq vector? rest composition))))
        ;; ""
        ]
    (if (= "" syls-str)
      lyrics-str
      syls-str)
    ))


(defn merge-with-lilypond-template[my-map]
  (let 
        [{:keys [version
                 title 
                 composer 
                 doremi-source
                 time-signature-snippet
                 key-signature-snippet
                 transpose-snippet
                 all-lyrics
                 staves
                 ]} my-map]
(strint/<< 
"#(ly:set-option 'midi-extension \"mid\")
\\version \"~{version}\"
\\include \"english.ly\"
\\header{ 
title = \"~{title}\" 
composer = \"~{composer}\" 
tagline = \"\"  % remove lilypond footer!!!
}

\\include \"english.ly\"

%{
    ~{doremi-source}
%}


melody =  {
		%  \\clef treble
%		\\cadenzaOn
    \\accidentalStyle modern-cautionary
		~{time-signature-snippet}
		~{key-signature-snippet}
    \\autoBeamOn  
		\\override Staff.TimeSignature #'style = #'()
~{staves}

}
\\score {
	\\new Staff <<
   ~{transpose-snippet} \\melody
   \\addlyrics {  ~{all-lyrics}
	 }
>>
\\layout { }
		\\midi {
				\\context {
						\\Staff
				}
				\\tempo 2 = 72
		}

}")))


(defn stave-to-lilypond
  ([stave] (stave-to-lilypond stave {}))
  ([stave context]
   (let [lyrics (staff-lyrics stave)
         v "23.5"
         my-map
         (merge context 
                { 
                 :melody (->> (conj stave [:eof]
                                    )
                              (tree-seq  #(and (vector? %)
                                               (#{:stave :lyrics-line :composition :notes-line :measure :beat} (first %)))
                                        identity)
                              (filter vector?)
                              (reduce lilypond-transition
                                      {:state :looking-for-attribute-section
                                       :finished-first-line false
                                       :in-slur (atom false)
                                       :syllables ""
                                       :source "" 
                                       :output ""
                                       :composition stave}
                                      )
                              :output 
                              )
                 :lyrics lyrics
                 } 
                )
         ]
     (when debug 
       (println "entering stave-to-lilypond, stave=" stave "lyrics=" lyrics))
     (strint/<< "~{(:melody my-map)}
                  \\break \\grace s16")))) 

(defn to-lilypond
  ([composition] (to-lilypond composition "")) 
  ([composition src]
   (when debug
     (println "entering to-lilypond, composition=" composition)
     )
   (assert (is? :composition composition))
   (let [ staves (filter #(is? :stave %) (rest composition))
         attributes (get-attributes composition)
         _ (when debug (println "**attributes=" attributes))
         context (merge attributes 
                        {
                         :all-lyrics (all-syls composition)
                         :version "2.18.2"
                         :time-signature-snippet (time-signature-snippet (:timesignature attributes))
                         :doremi-source (lilypond-escape src)

                         :transpose-snippet
                         (when (not (is-abc-composition attributes))
                           (transpose-snippet (:key attributes)))

                         :key-signature-snippet (key-signature-snippet 
                                                  attributes
                                                  )
                         }) 
         stave-data (clojure.string/join "\n" (map #(stave-to-lilypond % context) staves))
         ]
     (merge-with-lilypond-template (assoc context :staves stave-data))
     ))) 

