(ns movile-tron.client
  (:require [clj-http.client :as client]
    [clojure.string :as str])
  (:use [clojure.repl :only [pst]]))
;; a biker strategy is a function of state * arena -> state 

(defn post [url data]
  (:body (client/post url {:form-params data :content-type :edn :as :clojure})))

(defn biker-client [tag strategy server-url]
  (let [answer (post server-url {:tag tag})
        biker-url (:url answer)]
    (loop [arena (:arena answer) state {:pos (:pos answer)}]
      (let [new-state (strategy state arena)
            new-arena (post biker-url (select-keys new-state [:pos :msg]))]
        (when new-arena (recur new-arena (dissoc new-state :msg)))))))



