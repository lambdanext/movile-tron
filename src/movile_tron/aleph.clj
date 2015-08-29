(ns movile-tron.aleph
  (:require
    [clojure.core.async :as async]
    [clojure.tools.logging :as log]
    [aleph.http :as http]
    [manifold.deferred :as manifold]))

(extend-protocol manifold/Deferrable
  clojure.core.async.impl.channels.ManyToManyChannel
  (to-deferred [ch]
    (let [d (manifold/deferred)]
      (async/take! ch
        (fn [msg]
          (if (instance? Throwable msg)
            (manifold/error! d msg)
            (manifold/success! d msg))))
      d)))

(def request (async/chan))
(def result (async/chan))

(defn handler
  [req]
  (manifold/->deferred
    (async/go
      (async/>! request req)
      (let [v (async/<! result)]
        (log/info "Value is " v)
        (if v
          {:status 200
           :headers {"content-type" "text/plain"}
           :body (pr-str v)}
          {:status 200
           :headers {"content-type" "text/plain"}
           :body "Nope"})))))

(def server)

(defn start-server!
  []
  (log/info "Setting up echo loop")
  (async/go-loop []
    (when-let [v (async/<! request)]
      (async/>! result v))
      (recur))
  (log/info "Starting server on port 8080")
  (alter-var-root #'server
                  (constantly
                    (http/start-server
                      handler {:port 8080})))
  (log/info "Done")
  ; o.O)
  (Thread/sleep Long/MAX_VALUE))

; FIXME: At the moment there is no way to call as from the repl.
(defn stop-server!
  []
  (.close server)
  (async/close! request)
  (async/close! result)
  (System/exit 0))
