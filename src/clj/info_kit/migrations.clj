;;
;; inspired by https://github.com/technomancy/syme/blob/master/src/syme/db.clj#L66-L119
;;
(ns info-kit.migrations
  (:require [clojure.java.jdbc :as sql]
            [info-kit.db :refer [h2-local]]
            [clojure.tools.logging :as log]
            [info-kit.conf :refer [load-res]])
  (:import (java.sql Timestamp)))



(defn load-sql [res]
  (load-res (str "sql/migrations/" res ".sql")))

(def db-spec h2-local)

(defn initial-schema []
  (->>
    (load-sql "initial-schema")
    (sql/db-do-commands db-spec)))

(defn add-instance-id []
  (sql/db-do-commands db-spec "ALTER TABLE instances ADD COLUMN instance_id VARCHAR"))

(defn add-shutdown-token []
  (sql/db-do-commands db-spec "ALTER TABLE instances ADD COLUMN shutdown_token VARCHAR"))

(defn add-dns []
  (sql/db-do-commands db-spec "ALTER TABLE instances ADD COLUMN dns VARCHAR"))

(defn add-region []
  (sql/db-do-commands db-spec "ALTER TABLE instances ADD COLUMN region VARCHAR"))

;; migrations mechanics

(defn run-and-record [db-conn migration]
  (log/infof "Running migration: %s" (:name (meta migration)))
  (migration)
  (sql/insert! db-conn "migrations" [:name :created_at]
                     [(str (:name (meta migration)))
                      (Timestamp. (System/currentTimeMillis))]))

(defn migrate [& migrations]
  (try
    (->>
      (sql/create-table-ddl "migrations"
                            [[:name :varchar "NOT NULL"]
                             [:created_at :timestamp
                              "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]])
      (sql/db-do-commands db-spec))
    (catch Exception _))
  (sql/with-db-transaction
    [db-conn db-spec]
    (let [has-run? (sql/query db-conn ["SELECT name FROM migrations"]
                              {:result-set-fn #(set (map :name %))})]
      (doseq [m migrations
              :when (not (has-run? (str (:name (meta m)))))]
        (run-and-record db-conn m)))))

(defn run-migration []
  (log/info "running migrations")
  (migrate #'initial-schema))
           ;#'add-instance-id
           ;#'add-shutdown-token
           ;#'add-dns
           ;#'add-region))
