(ns info-kit.logging
  (:require [taoensso.timbre :as log]
            [clojure.java.jdbc :as jdbc]
            [info-kit.db :as db])
  (:import (java.sql Timestamp)
           (java.util Date)))

(defn log-message [config data]
  (let [entry
        {:instant   (Timestamp. (.getTime ^Date (:instant data)))
         :level     (str (:level data))
         :namespace (str (:?ns-str data))
         :msg       (str (force (:msg_ data)))}]

    (jdbc/with-db-connection
      [conn config]
      (jdbc/insert! conn :logs entry))))

(def h2-appender
  {:enabled?   true
   :async?     false
   :min-level  nil
   :rate-limit nil
   :output-fn  :inherit
   :fn         (fn [data] (log-message db/h2-local data))})

(defn config-logging []
  (log/set-level! :debug)
  (log/merge-config! {:appenders {:h2 h2-appender}}))






