(ns movile-tron.smartass)

(defn left  [[x y]] [(inc x) y])
(defn right [[x y]] [(dec x) y])
(defn up    [[x y]] [x (dec y)])
(defn down  [[x y]] [x (inc y)])

(defn path-length
  [direction current-position arena]
  (->> (iterate direction current-position)
    next
    (take-while (fn [p] (not (contains? arena p))))
    count))

(defn smartass
  [state arena]
  (let [best-dir (max-key #(path-length % (:pos state) arena)
                          left right up down)]
    ; (assoc state :pos (best-dir (get state :pos)))
    (update-in state [:pos] best-dir)))






(defn real-smartass
  [state arena]
  (let [path-length (fn [direction]
                      (->> (iterate direction (:pos state))
                        next
                        (take-while (fn [p] (not (contains? arena p))))
                        count))
        best-dir (max-key path-length left right up down)]
    ; (assoc state :pos (best-dir (get state :pos)))
    (update-in state [:pos] best-dir)))
