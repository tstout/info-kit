(ns info-kit.db
  (:require [taoensso.timbre :as log]
            [mount.core :refer [defstate]]
            [clojure.java.jdbc :as jdbc])
  (:import (java.net InetAddress)
           (org.h2.tools Server)))

(defn host-name []
  (let [host (.. InetAddress getLocalHost getHostName)]
    (log/infof "Using host name %s for DB..." host)
    host))

(defstate h2-local
          :start {:classname   "org.h2.Driver"
                  :subprotocol "h2"
                  :subname     (format "tcp://%s/~/.info-kit/db/info-kit;jmx=true" (host-name))
                  :user        "sa"
                  :password    ""})

(defstate h2-mem
          :start
          {:classname   "org.h2.Driver"
           :subprotocol "h2"
           :subname     "mem:info-kit;DB_CLOSE_DELAY=-1"
           :user        "sa"
           :password    ""})

(defn start-h2
  "Start a local H2 TCP Server"
  []
  (log/info "starting h2...")
  (->
    (into-array String ["-tcpAllowOthers"])
    Server/createTcpServer
    .start))

(defstate h2-db
          :start (start-h2)
          :stop (.stop h2-db))

(defn run-query
  "Execute the specified sql with an assumed id query parameter."
  [sql db-spec]
  (jdbc/with-db-connection [conn db-spec]
                           (jdbc/query conn [sql])))