(ns doremi-script.sargam-key-map
  (:require	
    [clojure.string :as string :refer [lower-case upper-case ]]
    ))

;; Provide a keymap for entering sargam letter notation. The AACM
;; sargam notation is case sensitive. To save typing, map characters
;; that are in sargam letters according to mode. S and P get automatically
;; mapped {"s" "S" "p" "P"} so that you can type a lower case "S" and it
;; is mapped to uppercase "S". The api takes a mode name (as a keyword) and
;; a string of notes used. A keymap (map) is returned. TODO: write test cases
(def debug false)

(enable-console-print!)

(def default-key-map 
    {"s" "S" "p" "P"})

(def mode->notes-used
  {
   :ionian "SRGmPDN"
   :dorian "SRgmPDn"
   :phyrgian "SrgmPdn"
   :lydian "SRGMPDN"
   :mixolydian "SRGmPDn"
   :aeolian "SRgmPdn"
   :locrian "SrgmPdn"

   :major "SRGmPDN"
   :minor "SRgmPdn"
   (keyword "harmonic minor") "SRgmPdN"
   :bhairav "SrGmPdN"
   :bilaval "SRGmPDN"
   :kafi "SRgmPDn"
   :bhairavi "SrgmPdn"
   :kalyan "SRGMPDN"
   :khammaj "SRGmPDn"
   :asavri "SRgmPdn"

   :marwa "SrGMPDN"
   :purvi "SrGMPdN"
   :lalit "SrGmMPdN"
   :hindol "SGMDN"
   :kirwani "SRgmPdN"
   (keyword "ahir bhairav") "SrGmPDn"
   }) 

(def lower-sargam? #{"s" "r" "g" "m" "p" "d" "n"})

(def upper-sargam? #{"S" "R" "G" "M" "P" "D" "N"})

(defn sargam-set->key-map[sargam-set]
  ;; Returns a keymap: ie {"s" "S" }. Saves typing
  ;; example (sargam-set->key-map #{R}) -> {"r" "R" "R" "r"}
  (assert (set? sargam-set))
  (when debug (println "in sargam-set-key-map, sargam-set=" sargam-set))
  (reduce (fn[accum item]
            (if (upper-sargam? item)
              (assoc accum 
                     item (lower-case item) 
                     (lower-case item) item)
              ;; else
              accum
              )) 
          {"s" "S" "p" "P"}  sargam-set)
  )

(defn remove-if-both-cases[my-set ch]
  (let [lower-ch (lower-case ch)
        upper-ch (upper-case ch)]
    (if (and (get my-set lower-ch)
             (get my-set upper-ch)
             )
      (clojure.set/difference my-set #{ lower-ch upper-ch })
      my-set
      )))


(defn notes-used->sargam-set[s]
  (assert (string? s))
    (set (reduce (fn[accum item] 
                   (when debug (println "accum,item" accum item))
                   (remove-if-both-cases (conj accum item) item))
                  #{} 
                 s))
    )

(defn mode-and-notes-used->key-map[mode notes-used]
  { :pre [(or (keyword? mode) (nil? mode))
          (or (string? notes-used) (nil? notes-used))
          ]
   :post [(map? %)]
   }
  (when debug (println "Entering mode-and-notes-used->key-map, mode, notes-used" mode notes-used))
    (->
      (or (mode->notes-used mode) notes-used "SP")
      notes-used->sargam-set 
      sargam-set->key-map)
    )

(comment
(println "**1 " (-> "SRGMPDnN" notes-used->sargam-set))
  (sargam-set->key-map #{"S" "R" "G" "m" "P" "D" "N"})
  (comment "test:  @key-map is" @key-map)
  )


