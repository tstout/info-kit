(ns info-kit.db
  (:require [clojure.tools.logging :as log])
  (:import (java.net InetAddress)
           (org.h2.tools Server)))

(defn host-name []
  (let [host (.. InetAddress getLocalHost getHostName)]
    (log/infof "Using host name %s for DB..." host)
    host))

(def h2-local
  {:classname   "org.h2.Driver"
   :subprotocol "h2"
   :subname     (format "tcp://%s/~/.info-kit/db/info-kit;jmx=true" (host-name))
   :user        "sa"
   :password    ""})

(def h2-mem
  {:classname   "org.h2.Driver"
   :subprotocol "h2"
   :subname     "mem:fin-kratzen;DB_CLOSE_DELAY=-1"
   :user        "sa"
   :password    ""})

(defn start-h2
  "Start a local H2 TCP Server"
  []
  (log/info "starting h2...")
  (let [h2Server (Server/createTcpServer (into-array String ["-tcpAllowOthers"]))]
    (.start h2Server)))

