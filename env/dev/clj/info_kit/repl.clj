(ns info-kit.repl
  (:use info-kit.handler
        figwheel-sidecar.repl-api
        ring.server.standalone
        [ring.middleware file-info file]))

(defonce server (atom nil))

(defn get-handler []
  ;; #'app expands to (var app) so that when we reload our code,
  ;; the server is forced to re-resolve the symbol in the var
  ;; rather than having its own copy. When the root binding
  ;; changes, the server picks it up without having to restart.
  (-> #'app
      ; Makes static assets in $PROJECT_DIR/resources/public/ available.
      (wrap-file "resources")
      ; Content-Type, Content-Length, and Last Modified headers for files in body
      (wrap-file-info)))

(defn start-server
  "used for starting the server in development mode from REPL"
  [& [port]]
  (println "Server Started-------")
  (let [port (if port (Integer/parseInt port) 3000)]
    (reset! server
            (serve (get-handler)
                   {:port port
                    :auto-reload? true
                    :join? false}))
    (println (str "You can view the site at http://localhost:" port))))

(defn stop-server []
  (.stop @server)
  (reset! server nil))

(defn load-vars []
  (require '[info-kit.db :as db]
           '[info-kit.migrations :as migrations] 
           '[clojure.java.jdbc :as sql]
           '[info-kit.conf :as conf]
           '[info-kit.db-io :as db-io]
           '[taoensso.timbre :as log]
           '[info-kit.logging :as log-cfg]
           '[info-kit.artifact :as artifact]
           '[info-kit.sys :as sys]
           '[clojure.pprint :refer [pprint]]))

(load-vars)
(println (str "Java Runtime: " (-> Runtime
                               type
                               .getPackage
                               .getImplementationVersion)))
