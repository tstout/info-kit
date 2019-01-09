;;
;; inspired by https://github.com/technomancy/syme/blob/master/src/syme/db.clj#L66-L119
;;
(ns info-kit.migrations
  (:require [clojure.java.jdbc :as sql]
            [info-kit.db :refer [h2-server db-conn]]
            [taoensso.timbre :as log]
            [info-kit.conf :refer [load-res]])
  (:import (java.sql Timestamp)))

(defn load-sql [res]
  (load-res (str "sql/migrations/" res ".sql")))

(defn initial-schema [conn]
  (->>
    (load-sql "initial-schema")
    (sql/db-do-commands conn)))

(defn add-tag-descr [conn]
  (sql/db-do-commands conn "ALTER TABLE TAGS ADD COLUMN description CLOB"))

(defn add-artifact-active [conn]
  (sql/db-do-commands conn "ALTER TABLE ARTIFACTS add COLUMN active boolean default true"))

(defn run-and-record [conn migration]
  ;;(log/infof "Running migration: %s" (:name (meta migration)))
  (migration conn)
  (sql/insert! conn "migrations" [:name :created_at]
               [(str (:name (meta migration)))
                (Timestamp. (System/currentTimeMillis))]))

(defn migrate [conn & migrations]
  (try
    (->>
      (sql/create-table-ddl "migrations"
                            [[:name :varchar "NOT NULL"]
                             [:created_at :timestamp
                              "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]])
      (sql/db-do-commands conn))
    (catch Exception _))
  (sql/with-db-transaction
    [db-conn conn]
    (let [has-run? (sql/query db-conn ["SELECT name FROM migrations"]
                              {:result-set-fn #(set (map :name %))})]
      (doseq [m migrations
              :when (not (has-run? (str (:name (meta m)))))]
        (run-and-record db-conn m)))))

(defn run-migration [env]
  (migrate (db-conn env)
           #'initial-schema
           #'add-tag-descr
           #'add-artifact-active))

(comment
  (in-ns 'info-kit.migrations)
  (run-migration :test)

  (def conn (info-kit.db/db-conn :test))
  (clojure.java.jdbc/query conn ["select * from migrations"])
  )