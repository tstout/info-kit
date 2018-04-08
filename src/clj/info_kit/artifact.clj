(ns info-kit.artifact
  (:require [clojure.edn :as edn]
            [info-kit.db-io :as db-io]
            [taoensso.timbre :as log]
            [ring.util.response :refer [response created not-found]]
            [info-kit.db :refer [h2-local]]))

(defn create-artifact
  "Create a new artifact. The request map must contain the following:
  {:name string
   :tags vector of tags to associate with the artifact
   :body string}
  "
  [request]
  (let [req-map (edn/read-string request)]
    (log/infof "Received create request: '%s'" (str req-map))
    (->>
      req-map
      (merge {:db-spec h2-local})
      db-io/new-artifact
      (str "artifact/")
      created)))

(defn update-artifact [])

(defn fetch-artifact [id]
  (log/infof "Received read request for artifact: %s" id)
  (if-let [result (db-io/read-artifact {:db-spec h2-local :id id})]
    (pr-str result)
    (not-found "artifact not found")))

(defn delete-artifact [id]
  (log/infof "Received delete request for artifact: %s" id)
  (db-io/delete-artifact {:db-spec h2-local :id id}))