(ns info-kit.server
  (:require [info-kit.handler :refer [app]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [taoensso.timbre :as log]
            [mount.core :as mount])
  (:gen-class))

 (defn -main [& args]
   (let [port (Integer/parseInt (or (env :port) "3000"))]
     (run-jetty app {:port port :join? false})
     (mount/start)))
