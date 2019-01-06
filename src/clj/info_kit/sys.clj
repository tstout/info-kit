(ns info-kit.sys
  (:require [info-kit.db :as db]
            [info-kit.logging :as logging]
            [info-kit.migrations :refer [run-migration]]))

(def server (db/mk-h2-server))

;;
;; Start/Stop the components comprising the service
;;
(defn start [env]
  (server :start)
  (run-migration env)
  (logging/init-logging env))

(defn stop [env]
  (server :stop))

