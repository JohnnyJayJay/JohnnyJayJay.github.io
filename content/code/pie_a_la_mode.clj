(ns pie-a-la-mode)

;; Recipe list
(def recipes
  {:pie {:pie-slice 1}
   :ice-cream {:ice-cream-scoop 2}
   :pie-a-la-mode {:pie-slice 1 :ice-cream-scoop 1}})

;; order map -> ingredient map
(defn order->ingredients [order]
  (->> order
       (map (fn [[item quantity]]
              (update-vals (get recipes item) (partial * quantity))))
       (apply merge-with +)))

;; ingredient map -> boolean
(defn enough-inventory? [inv ingredients]
  (every? (fn [[ingredient quantity]]
            (<= quantity @(get inv ingredient)))
          ingredients))

(defn handle-order
  [inv order]
  (let [ingredients (order->ingredients order)]
    (dosync
     (let [enough? (enough-inventory? inv ingredients)]
       (when enough?
         (doseq [[ingredient quantity] ingredients]
           (alter (get inv ingredient) - quantity)))
       enough?))))

;; Example Inventory of ingredients
(def sample-inventory
  {:pie-slice (ref 12)
   :ice-cream-scoop (ref 50)})

(defn random-order [] ;; => {:ice-cream 1}
  {(rand-nth [:pie :ice-cream :pie-a-la-mode]) (inc (rand-int 3))})

(run!
 deref
 (for [_ (range 100)]
   (future
     (let [order (random-order)
           result (handle-order sample-inventory order)]
       ;; Semafore while printing, to prevent the output from being mangled
       (locking *out*
         (println order "--->" result))))))
