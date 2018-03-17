(ns info-kit.db-io
  (:require [taoensso.timbre :as log]
            [mount.core :refer [defstate]]
            [clojure.java.jdbc :as jdbc]))

(defn add-tag [tag-name db-spec]
  (jdbc/with-db-connection
    [conn db-spec]
    (jdbc/insert! conn :tags {:name tag-name})))

(defn artifacts-by-tag [tag-name db-spec]
  (jdbc/with-db-connection
    [conn db-spec]
    (jdbc/query conn ["select id, name, count from tags where name = ?" tag-name])))