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

(defn insert-artifact
  [m]
  (let [{:keys [name body conn]} m]
    (->>
      (jdbc/insert! conn :artifacts {:body body :name name})
      first
      ((keyword "scope_identity()")))))

(defn assoc-tags [conn tags artifact-id]
  (doseq [t tags]
    (jdbc/insert!
      conn
      :artifact_tags
      {:artifact_id artifact-id
       :tag_id      t})
    (jdbc/execute! conn ["UPDATE tags SET count = count + 1 WHERE id = ?" t]))
  artifact-id)

(defn new-artifact
  "Create a new artifact based on the specified map.
  Returns the synthetic key of the freshly-minted artifact.
  Expected shape of the map defining the artifact:
  {:db-spec {map of database spec}
   :body    string defining body of artifact
   :name    string defining name of artifact>
   :tags    [vector of tag ids to associate with the artifact]}"
  [m]
  (let [{:keys [db-spec tags]} m]
    (jdbc/with-db-transaction
      [conn db-spec]
      (->>
        (insert-artifact (merge m {:conn conn}))
        (assoc-tags conn tags)))))