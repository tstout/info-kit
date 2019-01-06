(ns info-kit.artifact-test
  (:require [expectations :refer :all])
  (:require [clojure.string :as str])
  (:require [info-kit.conf :refer [load-res]])
  (:require [info-kit.migrations :refer [run-migration]])
  (:require [info-kit.db :refer [db-conn]])
  (:require [info-kit.artifact :refer :all]))

(defn in-context
  "rebind a var, expectations are run in the defined context"
  {:expectations-options :in-context}
  [work]
  (with-redefs [info-kit.artifact/*env* :test]
    (work)))


(defn db-migrate
  "Run in-memory DB migration"
  {:expectations-options :before-run}
  []
  (run-migration :test))


(def fixtures
  {:create-req (load-res "create-artifact.json")})

;;
;; Creating an artifact should return a location header
;; identifying the new artifact.
;;
(expect (more-of {:keys [status headers]}
                 201 status
                 true (contains? headers "Location"))
        (create-artifact (:create-req fixtures)))
;;
;; An artifact can be created and then fetched
;;
(expect (more-of {:keys [name body created]}
                 "artifact name" name
                 "body text" body
                 false (str/blank? created))
        (let [create-resp (create-artifact (:create-req fixtures))
              location (artifact-location create-resp)
              id (artifact-id location)]
              (from-json (fetch-artifact id))))

;;
;; An artifact can be created and then updated
;;
(expect (more-of {:keys [body]}
                 "Updated text!!!!!" body)
        (let [create-resp (create-artifact (:create-req fixtures))
              location (artifact-location create-resp)
              id (artifact-id location)]
              (update-artifact (to-json {:id id :body "Updated text!!!!!"}))
              (from-json (fetch-artifact id))))