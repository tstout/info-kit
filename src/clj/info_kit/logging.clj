(ns info-kit.logging
  (:require [taoensso.timbre :as log]
            [clojure.java.jdbc :as jdbc]
            [mount.core :refer [defstate]]
            [info-kit.db :refer [h2-local]])
  (:import (java.sql Timestamp)
           (java.util Date)))

(defn log-message [config data]
  (let [{:keys [instant level ?ns-str msg_]} data
        entry
        {:instant   (Timestamp. (.getTime ^Date instant))
         :level     (str level)
         :namespace (str ?ns-str)
         :msg       (str (force msg_))}]

    (jdbc/with-db-connection
      [conn config]
      (jdbc/insert! conn :logs entry))))

(def h2-appender
  {:enabled?   true
   :async?     false
   :min-level  nil
   :rate-limit nil
   :output-fn  :inherit
   :fn         (fn [data] (log-message h2-local data))})

(defn config-logging []
  (log/set-level! :debug)
  (log/merge-config! {:appenders {:h2 h2-appender}})
  (log/info "Logging Initialized"))

(defstate logging :start config-logging)




