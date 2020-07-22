(ns rest.core
  (:require[ajax.core :refer [GET POST PUT DELETE]]
           [ajax.edn :as e]
           [ajax.json :as ajax-json]
           [reagent.core :as r]
           [reagent.dom :as rdom]
           [clojure.string :as str]
           [reagent-modals.modals :as reagent-modals]
           [re-frame.core :as rf]
           [reagent-forms.core :as ref]
           [rest.events]))

(defn atom-input [val]
  [:input {:type "text"
           :value @val
           :on-change #(reset! val (-> % .-target .-value))}])

(defn row [label input]
  [:div.row
   [:div.col-md-2 [:label label]]
   [:div.col-md-5 input]])

(def form-template
  [:div
   (row "Full Name"
        [:input {:field :text :id :name
                 :validator (fn [doc]
                              (when (-> doc :name empty?)
                                ["error"]))}])
   (row "Gender "
        [:div.btn-group {:field :single-select :id :male}
         [:button.btn.btn-default {:key "M"} "M"]
         [:button.btn.btn-default {:key "F"} "F"]])
   (row "Date of birthday Y-M-D"
        [:input {:field
                                        ;:typedate
                 :datepicker
                 :id :dateofb
                 :date-format (fn [{:keys [year month day]}]
                                (str year "-" month "-" day))}])
   (row "Address "
        [:textarea {:field :textarea :id :address}])
   (row "Polic "
        [:input {:field :text :id :policynumber}])])

(def atom-person (r/atom {}))

(def atom-people (r/atom nil))

(def atom-change-person (r/atom {}))

(defn form-add-people []
  (let [person atom-person]
    (fn []
      [:div
       [ref/bind-fields form-template person]
       [:label (str @person)]])))

(defn form-change-person [p]
    (fn []
      [:div
       [ref/bind-fields form-template p]
       [:label (str @p)]]))

(defn modal-add-people []
  (#(reagent-modals/modal!
     [:div
      [:div {:class "modal-header"}
       [:h5 {:class "modal-title"} "Add people"]
       [:button {:type "button" :class "close"
                 :data-dismiss "modal"
                 :aria-label "Close"}
        [:span {:aria-hidden "true"} "×"]]]
      [:div {:class "modal-body"}
       [form-add-people]
       [:div.btn.btn-primary
        {:on-click (fn [_](rf/dispatch [:upsert-people @atom-person]))
         :type :submit }"Save"]]]){:size :lg}))

(defn modal-change-person [person]
  (let [p atom-change-person]
    (reset! p person)
    (#(reagent-modals/modal!
       [:div
        [:div {:class "modal-header"}
         [:h5 {:class "modal-title"} "Changed person"]
         [:button {:type "button" :class "close"
                   :data-dismiss "modal"
                   :aria-label "Close"}
          [:span {:aria-hidden "true"} "×"]]]
        [:div {:class "modal-body"}
         [(fn [](form-change-person p))]
         [:div.btn.btn-primary
          {:on-click (fn [_](rf/dispatch [:upsert-people @p]))
           :type :submit} "Save"]]]){:size :lg})))

(defn btn-add-people []
  [:div.btn.btn-primary
   {:on-click modal-add-people} "Add People"])

(defn btn-change-person [person]
  [:div.btn.btn-warning
   {:on-click (fn [] (modal-change-person person))} "Changed"])

(defn btn-deleted-person [per_id]
  (.log js/console (str per_id))
  [:div.btn.btn-danger
   {:on-click (fn [] (rf/dispatch [:delete-people per_id]))} "Deleted"])

(defn transform-date [date]
  (zipmap [:year :month :day] (map js/parseInt (str/split date #"-0?"))))


(defn get-person-data [people]
  (let [p people]
    (if @p
      (swap! people update-in [p] merge {:day 5 :month 5 :year 1994}))
    p))
    ;(.log js/console (str @people))))

(defn list-key [list]
  (into [] (map (fn [[k v]] v) list)))

(defn table-people []
  (rf/dispatch[:get-people])
  (fn []
    [:div
     [reagent-modals/modal-window]
     [btn-add-people]
     [:table {:class "table"}
      [:thead
       [:tr
        [:th "#"]
        [:th "Full Name"]
        [:th "Gender"]
        [:th "Date Y-M-D"]
        [:th "Address"]
        [:th "Polis"]
        [:th "Action"]]]
      [:tbody
       (for [p @(rf/subscribe [:people-list])]
         [:tr
          [:th (:per_id p)]
          [:th (:name p)]
          [:th (:male p)]
          [:th (clojure.string/join "-" (list-key (:dateofb p)))]
          [:th (:address p)]
          [:th (:policynumber p)]
          [:th
           [btn-change-person p]
           [btn-deleted-person (:per_id p)]]])]]]))

(defn ui []
  [:div
   [:h1 "People List"]
   [table-people]])

(defn render []
  (rdom/render [ui] (js/document.getElementById "app")))

(defn ^def/after-load clear-cache-and-render!
  []
  (rf/clear-subscription-cache!)
  (render))

(defn ^:export run []
  (rf/dispatch-sync [:initialize])
  (render))
