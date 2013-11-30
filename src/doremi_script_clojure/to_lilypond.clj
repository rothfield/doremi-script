(ns doremi_script_clojure.to_lilypond
  (:require	
    [clabango.parser :refer [render]]
    [clojure.string :refer [join]] 
    [clojure.pprint :refer [pprint]] 
    ))
(defn sample-data[]
  (read-string (slurp (clojure.java.io/resource "fixtures/sample-data.clj"))))




;; barline looks like this
(comment
    {:is_barline true,
     :my_type :single_barline,
     :source "|",
     :start_index 5}
)

(defn lookup-lilypond-barline[barline-type]
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

(def lilypond-octave-map
  {
   "-2 "","
   "-1 """
   "0 ""'"
   "1 " "''"
   "2" "'''"
   })

(defn line-to-lilypond-array[line & options]
  [""]  ;; TODO
  )
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


(defn calculate-lilypond-duration[numerator denominator]
  "TODO: rewrite"
  (if (= numerator denominator)
    "4"
    ;; else
    (let [frac (str numerator "/" denominator)
          ;;_ (println frac)
          looked-up-duration (fraction-to-lilypond frac)
          ;;_ (println looked-up-duration)
          ]
      (if looked-up-duration
        looked-up-duration
        ;; else
        "16"
        ))))

;;      frac=";;{numerator}/#{denominator}"
;;      looked-up-duration=fraction-to-lilypond[frac]
;;      if !looked-up-duration?
;;        alternate="16"
;;        return alternate ;; return something
;;      looked-up-duration

;;(pprint (calculate-lilypond-duration 4 6))

(defn get-ornament[pitch]
  (some #(if (= :ornament (:my_type %)) %) (:attributes pitch)))

(defn has-mordent[pitch]
  (some #(if (= :mordent (:my_type %)) %) (:attributes pitch)))


(def pitch-with-after-ornament
  {:syllable nil,
   :chord nil,
   :fraction_array [{:numerator 1, :denominator 1}],
   :pitch_source "S",
   :normalized_pitch "C",
   :numerator 1,
   :start_index 4,
   :denominator 1,
   :octave 0,
   :beat-counter 2,
   :my_type :pitch,
   :attributes
   [{:placement :after,
     :ornament_items
     [{:pitch_source "R",
       :normalized_pitch "D",
       :start_index 1,
       :pointer true,
       :octave 0,
       :beat-counter 0,
       :my_type :pitch,
       :pitch-counter 0,
       :source "R",
       :value :R}
      {:pitch_source "G",
       :normalized_pitch "E",
       :start_index 2,
       :pointer true,
       :octave 0,
       :beat-counter 1,
       :my_type :pitch,
       :pitch-counter 1,
       :source "G",
       :value :G}],
     :usable_source "RG",
     :start_index 1,
     :source "RG",
     :my_type :ornament}],
   :pitch-counter 2,
   :column_offset 0,
   :tala nil,
   :source "S",
   :value :S}
  )

(defn has-after-ornament[pitch]
  ;; (pprint pitch)
  (if (not= :pitch (:my_type pitch))
    false
    ;; else
    (= :after (:placement (get-ornament pitch)))  
    ))
(get-ornament pitch-with-after-ornament)
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

(def sample-beat1
  {:subdivisions 4,
   :start_index 0,
   :source "----",
   :my_type :beat,
   :items
   [{:fraction_array [{:numerator 1, :denominator 1}],
     :numerator 4,
     :start_index 0,
     :denominator 4,
     :beat-counter 0,
     :my_type :pitch,
     :rest true,
     :pitch-counter 0,
     :source "-",
     :dash_to_tie false}
    {:ignore true, :my_type :dash, :source "-", :start_index 1}
    {:ignore true, :my_type :dash, :source "-", :start_index 2}
    {:ignore true, :my_type :dash, :source "-", :start_index 3}]}
  )
(beat-is-all-dashes sample-beat1)
;;      fun = (item) ->
;;        return true if !item.my-type?
;;        return true if item.my-type is "dash"
;;        return false if item.my-type is "pitch"
;;        return true

(defn is-abc-line[line]
  (if (nil? (:kind line))
    false
    ;; else
    (re-find #"abc" (:kind line))))

(defn notation-is-in-abc[composition-data]
  (some is-abc-line (:lines composition-data)))

;;; (pprint (notation-is-in-abc (sample-data)))
(def lilypond-pitch-map
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

(defn lilypond-transpose[composition-data]
  "return transpose snippet for lilypond"
  "Don't transpose non-sargam. TODO:allow 123  "
  (cond (= "C" (:key composition-data))
        ""
        (notation-is-in-abc composition-data)
        ""
        true
        (str "\\transpose c' " (lilypond-pitch-map (:key composition-data)))))

(defn- my-seq2[x]
  (tree-seq (fn branch[node](or (map? node) (vector? node)))
            (fn children[node]
              (or (:items node) (:lines node) (:attributes node)
                  (:attributes node) node))
            x))

(defn extract-lyrics[x]
  (map :syllable (filter :syllable (my-seq2 x))))

(defn line-to-lilypond-array[line]
  ;; TODO
  ;;    # Line is a line from the parsed doremi_script
  (let [ in-slur (atom false)
        ]
    []
    ))

(defn line-to-lilypond[line & options]
  (join " " (line-to-lilypond-array line)))


(defn to-lilypond[template x]
  :pre (map? x)
  "Takes parsed doremi-script and returns lilypond text"
  ""
  (let [ src "source here"
        ]
  ;;(println "lyrics")
  ;;(println (extract-lyrics x))
  ;;(println "lines line-to-lilypond-array")
  ;;(pprint (mapcat line-to-lilypond-array (:lines x)))
  (render template 
          {:transpose-snip (lilypond-transpose x) 
           :extracted-lyrics (apply str (join " " (extract-lyrics x)))
           :beats-per-minute 200
           :title-snippet ""
           :src-snippet (str  "%{\n" src "\n%}\n")
           :notes "a'8"
           :time "4/4"
           })))

;;(pprint (filter :syllable (my-seq2 (sample-data))))

;;  (if false
;;  (pprint (to-lilypond 
;;            (-> "lilypond_templates/lilypond.txt" 
;;                clojure.java.io/resource 
;;                slurp)
;;            (doremi_script_clojure.core/sample-data)))
;;  )


(comment
  (to-lilypond "notmuch" (doremi_script_clojure.core/sample-data))
  )





;; Messy code from coffeescript version to
;;
;; process a line.
;;
;;  line-to-lilypond-array = (line,options={}) ->
;;    # Line is a line from the parsed doremi-script
;;    # Returns an array of items - join them with a string
;;    in-slur=false
;;    ary=[]
;;    in-times=false #hack
;;    at-beginning-of-first-measure-of-line=false
;;    dashes-at-beginning-of-line-array=[]
;;    tied-array=[]
;;    at-beginning-of-first-measure-of-line=false
;;    in-times=false #hack
;;    #@log "processing #{line.source}"
;;    all=[]
;;    x=root.all-items(line,all)
;;    last-pitch=null
;;    for item in all
;;      if item-has-attribute(item,"begin-slur") 
;;        in-slur=true
;;  
;;      if item.my-type in ["pitch","barline","measure"] or item.is-barline
;;        emit-tied-array(last-pitch,tied-array,ary) if tied-array.length >0 
;;  
;;      # TODO refactor
;;      # TODO: barlines should get attributes like endings too and talas too!
;;      if in-times
;;        if item.my-type is "beat" or item.my-type is "barline"
;;          ary.push "}"
;;          in-times=false
;;      #@log "processing #{item.source}, my-type is #{item.my-type}"
;;      if item.my-type=="pitch"
;;        last-pitch=item  #use this to help render ties better(hopefully)
;;        if dashes-at-beginning-of-line-array.length > 0
;;          for dash in dashes-at-beginning-of-line-array
;;            ary.push normalized-pitch-to-lilypond(dash,{last-pitch:last-pitch})
;;          dashes-at-beginning-of-line-array=[]
;;        ary.push normalized-pitch-to-lilypond(item,{in-slur:  in-slur,last-pitch:last-pitch})
;;      if item.is-barline
;;        ary.push(lookup-lilypond-barline(item.my-type))
;;      if item.my-type is "beat"
;;         beat=item
;;         if beat.subdivisions not in [0,1,2,4,8,16,32,64,128] and !beat-is-all-dashes(beat)
;;             #@log "odd beat.subdivisions=",beat.subdivisions
;;             x=2
;;             if beat.subdivisions is 6
;;               x=4
;;             if  beat.subdivisions is 5
;;               x=4
;;             disable-tuplet-numbers="\\override TupletNumber #'stencil = ##f"
;;             ary.push "#{disable-tuplet-numbers} \\times #{x}/#{beat.subdivisions} { "
;;             in-times=true #hack
;;      if item.my-type is "dash"
;;        if !item.dash-to-tie and item.numerator? #THEN its at beginning of line!
;;          #@log "pushing item onto dashes-at-beginning-of-line-array"
;;          dashes-at-beginning-of-line-array.push item
;;        if item.dash-to-tie
;;          #TODO:review
;;          console.log "dash-to-tie case!!***" if false
;;          console.log "dash-to-tie case!!***last-pitch is",last-pitch if false
;;          ary.push normalized-pitch-to-lilypond(item,{last-pitch: last-pitch})
;;          item=null
;;      if item? and item.my-type is "measure"
;;         measure=item
;;         if measure.is-partial
;;           # Lilypond partial measures:
;;           # \partial duration
;;           # where duration is the rhythmic length of the interval 
;;           # before the start of the first complete measure:
;;           ary.push "\\partial 4*#{measure.beat-count} "
;;      if item? and item.dash-to-tie
;;        tied-array.push item if item?
;;      # TODO: why/how can item be null???
;;      if item? and item-has-attribute(item,"end-slur") 
;;        in-slur=false
;;    if in-times
;;      ary.push "}"
;;      in-times=false
;;    console.log "tied-array",tied-array.length if false
;;    emit-tied-array(last-pitch,tied-array,ary) if tied-array.length >0 
;;    ary.push "\\break\n"
;;    ary
;;  
;;  
;;  
;;  
;;  
;;  
;;  
;; Note that the parser produces something like this for
;; -- --S- 
;;
;; composition
;;   line
;;     measure
;;       beat
;;         dash 
;;           numerator:2
;;           denominator:2
;;           rest: true #### NOTICE ###
;;           source: "-"
;;         dash
;;           source: "-"
;;       whitespace
;;       beat
;;         dash
;;           numerator:2
;;           denominator:4
;;           source: "-"
;;         dash
;;           source: "-"
;;         pitch:
;;           source: "S"
;;           numerator:2
;;           denominator:2
;;         dash:
;;           source: "-"
;;
;;
;;   So that the parser has marked off 1 1/2 beats as rests
;;   Note that Sargam only has rests at the beginning of a line by
;;   my interpretation!!

;;    item-has-attribute = (item,attr-name) ->
;;      return false if  !item.attributes?
;;      -.detect item.attributes,  (attr) ->
;;        return false if !attr.my-type?
;;        attr.my-type is attr-name
;;    
;;    extract-lyrics= (composition-data) ->
;;      ary=[]
;;      for sargam-line in composition-data.lines
;;        for item in root.all-items(sargam-line,[])
;;          #@log "extract-lyrics-item is",item
;;          ary.push item.syllable if item.syllable
;;      ary
;;    
;;    get-attribute= (composition-data,key) ->
;;      return null if !composition-data.attributes
;;      att=root.-.detect(composition-data.attributes.items, (item) ->
;;        item.key is key
;;        )
;;      return null if !att
;;      att.value
;;    
;;    log= (x) ->
;;      return if !@debug?
;;      return if !@debug
;;      console.log arguments... if console
;;    
;;    running-under-node= ->
;;      module? && module.exports
;;    
;;    my-inspect= (obj) ->
;;      return if ! debug?
;;      return if !debug
;;      return if !console?
;;      if running-under-node()
;;        console.log(util.inspect(obj,false,null)) 
;;        return
;;      console.log obj
;;    
;;    
;;    
;;    get-ornament = (pitch) ->
;;      return false if !pitch.attributes?
;;      root.-.detect(pitch.attributes, (attribute) -> attribute.my-type is "ornament")
;;      
;;    
;;    lookup-lilypond-pitch= (pitch) ->
;;      lilypond-pitch-map[pitch.normalized-pitch]
;;    
;;    lilypond-grace-note-pitch = (pitch) ->
;;      # generate a single pitch for use as a grace note
;;      duration="32"
;;      lilypond-pitch=lookup-lilypond-pitch(pitch)
;;      lilypond-octave=lilypond-octave-map["#{pitch.octave}"]
;;      return "???#{pitch.octave}" if !lilypond-octave?
;;      "#{lilypond-pitch}#{lilypond-octave}#{duration}"
;;    
;;    lilypond-grace-notes = (ornament,suppress-slurs=true) ->
;;      # TODO: review whether there should be a slur or not
;;      # generate a series of grace notes for an ornament
;;      #  c1 \afterGrace d1( { c16[ d]) } c1
;;      #  In the above line, generate what is between {}
;;      ary=(lilypond-grace-note-pitch(pitch) for pitch in ornament.ornament-items)
;;      
;;      needs-beam = (ary.length > 1)
;;      begin-beam=end-beam=""
;;      begin-slur="("
;;      begin-slur=""
;;      end-slur=")"
;;      if suppress-slurs
;;        begin-slur=""
;;        end-slur=""
;;      if needs-beam
;;        begin-beam="["
;;        end-beam="]"
;;      ary[0]= "#{ary[0]}#{begin-slur}#{begin-beam}" 
;;      length=ary.length
;;      ary[length-1]="#{ary[length-1]}#{end-slur}#{end-beam}" 
;;      # TODO: end slur??????????
;;      ary.join ' '
;;    
;;    get-chord= (item) ->
;;      if e =root.-.detect(item.attributes, (x) -> x.my-type is "chord-symbol")
;;        return """
;;        ^"#{e.source}"
;;        """
;;      ""
;;    
;;    get-ending= (item) ->
;;      if e =root.-.detect(item.attributes, (x) -> x.my-type is "ending")
;;        return """
;;        ^"#{e.source}"
;;        """
;;      ""
;;    
;;    normalized-pitch-to-lilypond= (pitch,context={last-pitch: {},in-slur:false}) ->
;;      special-case=false
;;      if pitch.dash-to-tie and has-after-ornament(context.last-pitch)
;;        console.log "***SPECIAL CASE" if false
;;        special-case=true
;;      console.log "context.in-slur is ", context.in-slur if false
;;      # Render a pitch/dash as lilypond
;;      # needs work
;;      chord=get-chord(pitch)
;;      ending=get-ending(pitch)
;;      if pitch.fraction-array?
;;        first-fraction=pitch.fraction-array[0]
;;      else
;;        first-fraction=new Fraction(pitch.numerator,pitch.denominator)
;;      duration=calculate-lilypond-duration first-fraction.numerator.toString(),first-fraction.denominator.toString()
;;      #@log("normalized-pitch-to-lilypond, pitch is",pitch)
;;      if pitch.my-type is "dash"
;;        # unfortunately this is resulting in tied 1/4s.
;;        if pitch.dash-to-tie is true
;;          pitch.normalized-pitch=pitch.pitch-to-use-for-tie.normalized-pitch
;;          pitch.octave=pitch.pitch-to-use-for-tie.octave
;;        else
;;          return "r#{duration}#{chord}#{ending}"
;;      lilypond-pitch=lilypond-pitch-map[pitch.normalized-pitch]
;;      return "???#{pitch.source}" if  !lilypond-pitch?
;;      lilypond-octave=lilypond-octave-map["#{pitch.octave}"]
;;      return "???#{pitch.octave}" if !lilypond-octave?
;;      # Lower markings would be added as follows:
;;      # "-\"#{pp}\""
;;      mordent = if has-mordent(pitch) then "\\mordent" else ""
;;      begin-slur = if item-has-attribute(pitch,"begin-slur") then "("  else ""
;;      end-slur  =  if item-has-attribute(pitch,"end-slur") then ")" else ""
;;      in-slur=false
;;      if item-has-attribute(pitch,"begin-slur")
;;        in-slur=true
;;      if item-has-attribute(pitch,"end-slur")
;;        in-slur=true
;;      console.log "in-slur is",in-slur if false
;;      suppress-slurs=in-slur
;;      if context.in-slur
;;        suppress-slurs=true
;;      lilypond-symbol-for-tie=  if pitch.tied? then '~' else ''
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
;;    
;;    emit-tied-array=(last-pitch,tied-array,ary) ->
;;      console.log "********emit-tied-array" if false
;;    
;;      return if !last-pitch?
;;      return if tied-array.length is 0
;;    
;;      my-funct= (memo,my-item) ->
;;        frac=new Fraction(my-item.numerator,my-item.denominator)
;;        if !memo?  then frac else frac.add memo
;;        
;;      fraction-total=-.reduce(tied-array,my-funct,null)
;;      
;;      obj={}
;;      for key of last-pitch
;;        obj[key]=last-pitch[key]
;;      # hack the obj attributes to remove mordents
;;     
;;      filter = (attr) ->
;;        attr.my-type? and attr.my-type is not "mordent"
;;      obj.attributes= -.select(last-pitch.attributes,filter)
;;      obj.numerator=fraction-total.numerator
;;      obj.denominator=fraction-total.denominator
;;      obj.fraction-array=null
;;      #TODO: make more general
;;      my-fun = (attr) ->
;;        attr.my-type is not "mordent"
;;      obj.attrs2= -.select(obj.attributes, my-fun)
;;      #@log "emit-tied-array-last is", last
;;      last=tied-array[tied-array.length-1]
;;      obj.tied= last.tied
;;      #@log "leaving emit-tied-array"
;;      tied-array.length=0 # clear it
;;      console.log "****",has-after-ornament(last-pitch) if false
;;      if has-after-ornament(last-pitch)
;;        # TODO: in this case, if the previous pitch had an after grace, then
;;        # add a right paren !!! April 14,2012
;;        console.log "ADD CODE TO ADD RIGHT PARENT TO THIS PITCH",obj if false
;;    
;;      ary.push normalized-pitch-to-lilypond(obj,{last-pitch:last-pitch})
;;     
;;    is-sargam-line= (line) ->
;;      return false if !line.kind?
;;      return true if line.kind.indexOf('sargam') > -1
;;      return true if line.kind.indexOf('number') > -1 # 3/10/2012
;;    
;;    notation-is-in-sargam= (composition-data) ->
;;      #@log "in notation-is-in-sargam"
;;      root.-.detect(composition-data.lines, (line) -> is-sargam-line(line))
;;    
;;    
;;    line-to-lilypond = (line,options={}) ->
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
;;      # Returns an array of items - join them with a string
;;      in-slur=false
;;      ary=[]
;;      in-times=false #hack
;;      at-beginning-of-first-measure-of-line=false
;;      dashes-at-beginning-of-line-array=[]
;;      tied-array=[]
;;      at-beginning-of-first-measure-of-line=false
;;      in-times=false #hack
;;      #@log "processing #{line.source}"
;;      all=[]
;;      x=root.all-items(line,all)
;;      last-pitch=null
;;      for item in all
;;        if item-has-attribute(item,"begin-slur") 
;;          in-slur=true
;;    
;;        if item.my-type in ["pitch","barline","measure"] or item.is-barline
;;          emit-tied-array(last-pitch,tied-array,ary) if tied-array.length >0 
;;    
;;        # TODO refactor
;;        # TODO: barlines should get attributes like endings too and talas too!
;;        if in-times
;;          if item.my-type is "beat" or item.my-type is "barline"
;;            ary.push "}"
;;            in-times=false
;;        #@log "processing #{item.source}, my-type is #{item.my-type}"
;;        if item.my-type=="pitch"
;;          last-pitch=item  #use this to help render ties better(hopefully)
;;          if dashes-at-beginning-of-line-array.length > 0
;;            for dash in dashes-at-beginning-of-line-array
;;              ary.push normalized-pitch-to-lilypond(dash,{last-pitch:last-pitch})
;;            dashes-at-beginning-of-line-array=[]
;;          ary.push normalized-pitch-to-lilypond(item,{in-slur:  in-slur,last-pitch:last-pitch})
;;        if item.is-barline
;;          ary.push(lookup-lilypond-barline(item.my-type))
;;        if item.my-type is "beat"
;;           beat=item
;;           if beat.subdivisions not in [0,1,2,4,8,16,32,64,128] and !beat-is-all-dashes(beat)
;;               #@log "odd beat.subdivisions=",beat.subdivisions
;;               x=2
;;               if beat.subdivisions is 6
;;                 x=4
;;               if  beat.subdivisions is 5
;;                 x=4
;;               disable-tuplet-numbers="\\override TupletNumber #'stencil = ##f"
;;               ary.push "#{disable-tuplet-numbers} \\times #{x}/#{beat.subdivisions} { "
;;               in-times=true #hack
;;        if item.my-type is "dash"
;;          if !item.dash-to-tie and item.numerator? #THEN its at beginning of line!
;;            #@log "pushing item onto dashes-at-beginning-of-line-array"
;;            dashes-at-beginning-of-line-array.push item
;;          if item.dash-to-tie
;;            #TODO:review
;;            console.log "dash-to-tie case!!***" if false
;;            console.log "dash-to-tie case!!***last-pitch is",last-pitch if false
;;            ary.push normalized-pitch-to-lilypond(item,{last-pitch: last-pitch})
;;            item=null
;;        if item? and item.my-type is "measure"
;;           measure=item
;;           if measure.is-partial
;;             # Lilypond partial measures:
;;             # \partial duration
;;             # where duration is the rhythmic length of the interval 
;;             # before the start of the first complete measure:
;;             ary.push "\\partial 4*#{measure.beat-count} "
;;        if item? and item.dash-to-tie
;;          tied-array.push item if item?
;;        # TODO: why/how can item be null???
;;        if item? and item-has-attribute(item,"end-slur") 
;;          in-slur=false
;;      if in-times
;;        ary.push "}"
;;        in-times=false
;;      console.log "tied-array",tied-array.length if false
;;      emit-tied-array(last-pitch,tied-array,ary) if tied-array.length >0 
;;      ary.push "\\break\n"
;;      ary
;;    
;;    to-lilypond= (composition-data,options={}) ->
;;      ary=[]
;;      for line in composition-data.lines
;;        ary= ary.concat line-to-lilypond-array line,options
;;      mode = composition-data.mode #get-mode(composition-data,'Mode')
;;      mode or= "major"
;;      mode=mode.toLowerCase()
;;      composer = get-attribute(composition-data,"Author")
;;      composer-snippet=""
;;      if composer
;;        composer-snippet= """
;;          composer = "#{composer}"
;;         """
;;    
;;      title = get-attribute(composition-data,"Title")
;;      time = get-attribute(composition-data,"TimeSignature")
;;      transpose-snip=lilypond-transpose(composition-data)
;;      # Don't transpose non-sargam notation TODO:review
;;      if ! notation-is-in-sargam(composition-data)
;;        transpose-snip=""
;;      time="4/4" if !time
;;      key-snippet= """
;;      \\key c \\#{mode}
;;      """
;;      if ! notation-is-in-sargam(composition-data)
;;        key-snippet= """
;;        \\key #{lilypond-pitch-map[composition-data.key]} \\#{mode}
;;        """
;;      
;;      title-snippet=""
;;      if title
;;        title-snippet= """
;;          title = "#{title}"
;;         """
;;      notes = ary.join " "
;;      # Anything that is enclosed in %{ and %} is ignored  by lilypond
;;      composition-data.source="" if !composition-data.source?
;;      src1= composition-data.source.replace /%\{/gi, "% {"
;;      src= src1.replace /\{%/gi, "% }"
;;    
;;      if options.omit-header
;;        title-snippet=composer-snippet=""
;;      # TODO: add to doremi-script and gui
;;      beats-per-minute=200
;;      lilypond-template= """
;;      #(ly:set-option 'midi-extension "mid")
;;      \\version "2.12.3"
;;      \\include "english.ly"
;;      \\header{ 
;;      #{title-snippet}
;;      #{composer-snippet}
;;      tagline = ""  % removed 
;;      }
;;    %{
;;    #{src}  
;;    %}
;;    melody = {
;;    \\clef treble
;;    #{key-snippet}
;;    \\time #{time}
;;    \\autoBeamOn  
;;    #{notes}
;;    }
;;    
;;    text = \\lyricmode {
;;      #{extract-lyrics(composition-data).join(' ')}
;;    }
;;    
;;    \\score{
;;    #{transpose-snip}
;;    <<
;;      \\new Voice = "one" {
;;        \\melody
;;      }
;;      \\new Lyrics \\lyricsto "one" \\text
;;    >>
;;    \\layout {
;;      \\context {
;;           \\Score
;;        \\remove "Bar-number-engraver"
;;      } 
;;      }
;;    \\midi { 
;;      \\context {
;;        \\Score
;;        tempoWholesPerMinute = #(ly:make-moment #{beats-per-minute} 4)
;;       }
;;     }
;;    }
;;      """
;;      lilypond-template
;;    
;;    
;;    ;;
