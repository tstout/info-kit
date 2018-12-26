(ns info-kit.server
  (:require [info-kit.handler :refer [app]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [taoensso.timbre :as log]
            [info-kit.sys :as sys])
  (:gen-class))


(defn -main [& args]
  (let [port (Integer/parseInt (or (env :port) "3000"))]
    (sys/start :prod)
    (log/infof "Starting jetty on port %d" port)
    (run-jetty app {:port port :join? false})))
