(ns doremi-script.sargam-key-map
  (:require	
    [clojure.string :as string :refer [lower-case upper-case ]]
    ))

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


(defn notes-used-set-for[{mode :mode notes-used :notes-used}]
  (let [
        mode-notes-used (when mode
                          (get mode->notes-used  (keyword (lower-case mode)) ""))
        notes-used2 (or  notes-used
                        mode-notes-used
                        "SP")

        ]
    (assert (string? notes-used2))
    (set (reduce (fn[accum item] (remove-if-both-cases accum item))
                 notes-used2
                 "rgmdn"))
    ))


(comment
  (sargam-set->key-map #{"S" "R" "G" "m" "P" "D" "N"})
  (comment "test:  @key-map is" @key-map)
  )

(defn mode-and-notes-used->key-map[mode notes-used]
    (->
      {:mode mode :notes-used notes-used}
      notes-used-set-for
      sargam-set->key-map)
  {}      
)

