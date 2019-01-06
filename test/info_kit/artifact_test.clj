(ns info-kit.artifact-test
  ;;(:require [clojure.test :refer :all])
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
(expect (more-of resp
                 201 (:status resp)
                 true (contains? (:headers resp) "Location"))
        (create-artifact (:create-req fixtures)))
;;
;; An artifact can be created and then fetched
;;
(expect true
        (let [create-resp (create-artifact (:create-req fixtures))
              location (artifact-location create-resp)]
          (str/starts-with? location "artifact")))
