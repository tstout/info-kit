(ns info-kit.db-io
  (:require [taoensso.timbre :as log]
            [info-kit.db :refer [mk-conn]]
            [info-kit.conf :refer [load-res]]
            [clojure.java.jdbc :as jdbc])
  (:import (java.io BufferedReader)))

(def queries
  { :artifacts-by-tag (load-res "sql/queries/select-artifacts-by-tag-id.sql")})

(defn add-tag [tag-name env]
  (jdbc/with-db-connection
    [conn (mk-conn env)]
    (jdbc/insert! conn :tags {:name tag-name})))

(defn artifacts-by-tag
  ""
  [tag-id env]
  (jdbc/with-db-connection
    [conn (mk-conn env)]
    (jdbc/query conn [(:artifacts-by-tag queries) tag-id])))


;; TODO - consider replacing this scope_identity with a dedicated
;; sequence.
(defn insert-artifact
  [m]
  (let [{:keys [name body conn]} m]
    (->>
      (jdbc/insert! conn :artifacts {:body body :name name})
      first
      ((keyword "scope_identity()")))))

(defn assoc-tags
  "associate tags with an existing artifact."
  ([conn tags artifact-id]
   (doseq [t tags]
     (jdbc/insert!
       conn
       :artifact_tags
       {:artifact_id artifact-id
        :tag_id      t})
     (jdbc/execute! conn ["UPDATE tags SET count = count + 1 WHERE id = ?" t]))
   artifact-id)
  ([m]
   {:pre [(map? m)]}
   (let [{:keys [env tags artifact-id]} m]
     (assoc-tags (mk-conn env) tags artifact-id))))

;; TODO - look into using spec to define the shape of maps here...
(defn new-artifact
  "Create a new artifact based on the specified map.
  Returns the synthetic key of the freshly-minted artifact.

  Expected shape of the map defining the artifact:
  {:body    string defining body of artifact
   :name    string defining name of artifact>
   :tags    [vector of tag ids to associate with the artifact]}"
  [m]
  (let [{:keys [tags env]} m]
    (jdbc/with-db-transaction
      [conn (mk-conn env)]
      (->>
        (insert-artifact (merge m {:conn conn}))
        (assoc-tags conn tags)))))

(defn update-artifact
  "Update an existing artifact.
  Expected shape of the map defining the artifact:
  {:db-spec {map of databse spec}
   :body    string defining body to update
   :id      primary key of the artifact}"
  [m]
  (let [{:keys [body id env]} m]
    (jdbc/with-db-transaction
      [conn (mk-conn env)]
      (jdbc/execute! conn ["update artifacts set body = ? where id = ?"
                           body
                           id]))))

(defn delete-artifact
  "Deletes an artifact.
  Expected shape of the map defining the artifact:
  {:db-spec {map of database spec}
  :id       primary key of the artifact}"
  [m]
  (let [{:keys [env id]} m]
    (jdbc/with-db-transaction
      [conn (mk-conn env)]
      (jdbc/execute! conn ["update artifacts set active = false where id = ?" id]))))

(defn clob-to-string [row]
  (assoc row :body
             (with-open
               [rdr (BufferedReader. (.getCharacterStream (:body row)))]
               (reduce
                 (fn [a b] (str a "\n" b))
                 (line-seq rdr)))))

(defn read-artifact
  "Retrieve a single artifact by id"
  [m]
  (let [{:keys [env id]} m]
    (-> (jdbc/with-db-connection
          [conn (mk-conn env)]
          (jdbc/query conn
                      ["select created, name, body from artifacts where id = ?" id]
                      {:row-fn clob-to-string}))
        first)))

