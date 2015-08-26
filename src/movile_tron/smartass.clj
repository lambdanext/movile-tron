(ns movile-tron.smartass)

(defn smartass
  [state arena]
  (let [left  (fn [[x y]] [(inc x) y])
        right (fn [[x y]] [(dec x) y])
        up    (fn [[x y]] [x (dec y)])
        down  (fn [[x y]] [x (inc y)])
        path-length (fn [direction]
                      (->> (iterate direction (:pos state))
                       next
                       (take-while
                         (fn [p] (not (contains? arena p))))
                       count))
        best-dir    (max-key path-length left right up down)]
    ; (assoc state :pos (best-dir (get state :pos)))
    (update-in state [:pos] best-dir)))
