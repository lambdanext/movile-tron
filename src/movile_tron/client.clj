(ns movile-tron.client
  (:require [clj-http.client :as client]
    [clojure.string :as str])
  (:use [clojure.repl :only [pst]]))
;; a biker strategy is a function of state * arena -> state 

(defn post [url data]
  (let [answer (client/post url
                 {:form-params data 
                  :content-type :edn 
                  :as :clojure
                  :follow-redirects true})
        url (or (peek (:trace-redirects answer)) url)]
    (when-let [body (:body answer)] (assoc body :url url))))

(defn biker-client [tag strategy server-url]
  (let [{:keys [arena pos url]} (post server-url {:tag tag})]
    (loop [arena arena state {:pos pos} url url]
      (let [new-state (strategy state arena)]
        (when-let [{:keys [arena url]} (post url (select-keys new-state [:pos :msg]))]
          (recur arena (dissoc new-state :msg) url))))))
