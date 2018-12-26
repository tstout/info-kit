(ns info-kit.artifact
  (:require [clojure.edn :as edn]
            [info-kit.db-io :as db-io]
            [taoensso.timbre :as log]
            [ring.util.response :refer [response created not-found]]))

(def ^:dynamic *env* :prod)

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
      (merge {:env *env*})
      db-io/new-artifact
      (str "artifact/")
      created)))

(defn update-artifact [])

(defn fetch-artifact [id]
  (log/infof "Received read request for artifact: %s" id)
  (if-let [result (db-io/read-artifact {:env *env* :id id})]
    (pr-str result)
    (not-found "artifact not found")))

(defn delete-artifact [id]
  (log/infof "Received delete request for artifact: %s" id)
  (db-io/delete-artifact {:env *env* :id id}))