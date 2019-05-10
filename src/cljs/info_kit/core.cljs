(ns info-kit.core
  (:require [reagent.core :as reagent :refer [atom]]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [info-kit.state :refer [evt-handler]]))

;; -------------------------
;; Views

;(defn home-page []
;  [:div [:h2 "Welcome to info-kit"]
;   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div [:h3 "Ad-hoc stash of information"]
   [:p "TODO - add version information"]])

(defn reminder-page []
  [:div [:h3 "Reminders"]])

(defn logs-page []
  [:div [:h3 "Logs"]])

(defn reporting-page []
  [:div [:h3 "Reporting"]])

(defn tags-page []
  [:div [:h3 "Tags"]])


;; -------------------------
;; Routes

(defonce page (atom #'tags-page))

(defn current-page []
  [:div [:header [:h1 "info-kit"]]
   [:div {:class :sidenav}
    [:a {:href "/"} "Tags"]
    [:a {:href "/reminders"} "Reminders"]
    [:a {:href "/logs"} "Logs"]
    [:a {:href "/reporting"} "Reporting"]
    [:a {:href "/about"} "About"]]
   [:div {:class :main}
    [@page]]
   [:footer "Info Kit"]])

(secretary/defroute "/" []
                    (reset! page #'tags-page))

(secretary/defroute "/about" []
                    (reset! page #'about-page))

(secretary/defroute "/logs" []
                    (reset! page #'logs-page))

(secretary/defroute "/reporting" []
                    (reset! page #'reporting-page))

(secretary/defroute "/reminders" []
                    (reset! page #'reminder-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root)
  (evt-handler))
