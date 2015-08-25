(ns movile-tron.core
  (:require [quil.core :as q]
    [clojure.string :as str])
  (:use [clojure.repl :only [pst]]))

(def size "size of the square arena" 100)
(def scale 7)
(def turn-duration "time in ms between turns" 100)

(defn square-arena [n]
  (let [n-1 (dec n)]
    (into {}
     (for [i (range 0 n-1)
           pt [[0 i] [n-1 (- n-1 i)]
               [(- n-1 i) 0]
               [i n-1]]]
       [pt :wall]))))

(def arena
  (atom (square-arena size)))

(def messages (atom []))

(defn blank-arena! []
  (reset! arena (square-arena size)))

(defn setup []
  (q/color-mode :hsb)
  (q/smooth)
  (q/frame-rate 10))

(defn draw []
  (q/background 0)
  (doseq [[[x y] tag] @arena]
    (q/fill (if (= :wall tag)
              (q/color 0 255 192)
              (q/color (bit-and 255 (hash tag)) 255 255)))
    (q/rect (* scale x) (* scale y) scale scale))
    (q/fill (q/color 255))
  (q/text (str/join "\n" (take 10 (rseq @messages))) (* 2 scale) (* 3 scale)))

(q/defsketch tron
  :title "TRON"
  :setup setup
  :draw draw
  :size [(* scale size) (* scale size)])

(def legal-moves #{[0 1] [1 0] [0 -1] [-1 0]})

(defn valid-move? [from to]
  (contains? legal-moves (map - to from)))

;; a biker strategy is a function of state * arena -> state 

(defn- move! [arena pos tag]
  (loop []
    (let [snap-arena @arena]
      (when-not (contains? snap-arena pos)
        (if (compare-and-set! arena snap-arena (assoc snap-arena pos tag))
          true
          (recur))))))

(defn remove-trail [arena tag]
  (reduce-kv (fn [arena k v]
               (if (= tag v) (dissoc arena k) arena))
    arena arena))

(defn biker-thread [tag state strategy]
  (doto (Thread.
          (fn [] 
            (loop [{pos :pos :as state} state]
              (let [start-time (java.lang.System/currentTimeMillis)
                    future-state (future (strategy state @arena))]
                (when-let [{new-pos :pos :as new-state} (deref future-state turn-duration nil)]
                  (when (and (valid-move? pos new-pos) (move! arena new-pos tag))
                    (java.lang.Thread/sleep 
                      (- turn-duration (- (java.lang.System/currentTimeMillis) start-time)))
                    (recur new-state)))))
            (swap! messages conj (str "RIP " tag))
            (swap! arena remove-trail tag)))
    .start))

(defn battle [strategies-map]
  (let [spawn-points (distinct (repeatedly (fn [] [(rand-int size)
                                                   (rand-int size)])))]
    (doseq [[[tag strategy] point] (map vector strategies-map spawn-points)]
      (biker-thread tag {:pos point} strategy))))
