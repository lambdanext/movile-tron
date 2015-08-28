(ns movile-tron.server
  (:require
    [clojure.string :as str]
    [ring.adapter.jetty :as jetty]
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [movile-tron.core :as core])
  (:use [clojure.repl :only [pst]]))

(def states (atom {}))

(defn web-bot [tag]
  (fn [state arena]
    (when-let [[args-promise ret-promise] (get @states tag)] 
      (deliver args-promise [state arena])
      (let [state' (deref ret-promise)]
        (swap! states assoc tag [(promise) (promise)])
        state'))))

;; kids don't do that at home, we should have used an async http task
(defn app [req]
  (let [payload (-> req :body io/reader java.io.PushbackReader. edn/read)]
    (if (= "/" (:uri req))
      (let [tag (gensym (name (:tag payload)))
            path (str "/" tag)
            ret-promise (promise)
            args-promise (promise)]
        (swap! states assoc path [args-promise ret-promise])
        (core/battle {tag (web-bot path)})
        {:status 303
         :headers {"location" path}
         :body "This is not the bot you are looking for."})
      (if-let [[args-promise ret-promise] (get @states (:uri req))]
        (do
          (when (= (:request-method req) :post)
            (deliver ret-promise (select-keys payload [:pos :msg])))
          (if-let [[{pos :pos} arena] (deref args-promise core/turn-duration nil)]
            {:status 200 :body (pr-str {:pos pos :arena arena})}
            {:status 200 :body (pr-str nil)}))
        {:status 404 :body "not found"}))))

(defonce server (jetty/run-jetty #'app {:join? false
                                        :max-threads 80
                                        :port 8080}))