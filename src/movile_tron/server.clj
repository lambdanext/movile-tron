(ns movile-tron.server
  (:require [quil.core :as q]
    [clojure.string :as str]
    [ring.adapter.jetty :as jetty]
    [clojure.edn :as edn]
    [clojure.java.io :as io])
  (:use [clojure.repl :only [pst]]))

(def size "size of the square arena" 50)
(def scale "size of a square in pixels" 7)
(def turn-duration "time (in ms) between turns" 300)

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

(def client-states (atom {}))

(defn setup []
  (q/color-mode :hsb)
  (q/smooth)
  (q/frame-rate 20))

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

;; kids don't do that at home, we should have used an async http task
(defn app [req]
  (let [payload (-> req :body io/reader java.io.PushbackReader. edn/read)]
    (if (= "/" (:uri req))
      (let [tag (gensym (:tag payload))
            pos (first (remove @arena (repeatedly (fn [] [(rand-int size) (rand-int size)]))))
            arena (swap! arena assoc pos tag)
            path (str "/" tag)
            url (str (name (:scheme req)) "://" (get-in req [:headers "host"]) path)]
        (swap! client-states assoc path [tag (java.lang.System/currentTimeMillis) pos])
        {:status 200
         :body (pr-str {:pos pos :arena arena :url url})})
      (if-let [[tag timestamp pos] (get @client-states (:uri req))]
        (or (let [new-pos (:pos payload) 
                  now (java.lang.System/currentTimeMillis)]
              (when (and (< (- now timestamp) turn-duration)
                      (valid-move? pos new-pos) (move! arena new-pos tag))
                (when-let [msg (:msg payload)]
                  (swap! messages conj (str tag " said " msg)))
                (java.lang.Thread/sleep 
                  (- turn-duration (- now timestamp)))
                (swap! client-states assoc (:uri req) [tag (java.lang.System/currentTimeMillis) new-pos])
                {:status 200
                 :body (pr-str @arena)}))
          (do
            (swap! messages conj (str "RIP " tag))
                (swap! arena remove-trail tag)
            {:status 200
             :body (pr-str nil)}))
        {:status 404 :body "not found"}))))

(defonce server (jetty/run-jetty #'app {:join? false
                                        :max-threads 80
                                        :port 8080}))