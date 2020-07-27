(ns rest.core
  (:require [rest.events]
            [rest.ui :as ui]
            [re-frame.core :as rf]
            [reagent.dom :as rdom]))

(defn main-ui []
  [:div
   [:h1 "People List"]
   [ui/table-people]])


(defn render []
  (rdom/render [main-ui] (js/document.getElementById "app")))

(defn ^def/after-load clear-cache-and-render!
  []
  (rf/clear-subscription-cache!)
  (render))

(defn ^:export run []
  (rf/dispatch-sync [:initialize])
  (render))
