(ns rest.core
  (:require[ajax.core :refer [GET POST PUT DELETE]]
           [ajax.edn :as e]
           [ajax.json :as ajax-json]
           [reagent.core :as r]
           [reagent.dom :as rdom]
           [clojure.string :as str]
           [reagent-modals.modals :as reagent-modals]
           [reagent-forms.core :as rf]))

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
   (row "Name"  [:input {:field :text :id :name
                         :validator (fn [doc]
                                      (when (-> doc :person :name :first empty?)
                                        ["error"]))}])
   (row "Gender " [:div.btn-group {:field :single-select :id :male}
                 [:button.btn.btn-default {:key "M"} "M"]
                 [:button.btn.btn-default {:key "F"} "F"]])
   (row "Date of birthday " [:input {:field
                                     :datepicker
                                     :id :dateofb
                                     :date-format (fn [{:keys [year month day]}]
                                                    (str year "-" month "-" day))}])
   (row "Address " [:textarea {:field :textarea :id :address}])
   (row "Polic " [:input {:field :text :id :policynumber}])])

(def form-template-change
  [:div
   (row "Name"  [:input {:field :text :id :name}])
   (row "Gender " [:div.btn-group {:field :single-select :id :male}
                   [:button.btn.btn-default {:key "M"} "M"]
                   [:button.btn.btn-default {:key "F"} "F"]])
   (row "Date of birthday " [:input {:field :datepicker :id :date :date-format "yyyy/mm/dd"}])
   (row "Address " [:textarea {:field :textarea :id :address}])
   (row "Polic " [:input {:field :text :id :policynumber}])])

(defn error-handler [{:keys [status status-text message]}]
  (.log js/console (str "something bad happened: " status " " status-text))
  (js/alert status-text))

(defn handler []
  (.reload js/window.location true))

(def atom-person (r/atom {}))

(def atom-people (r/atom nil))

(def atom-change-person (r/atom {}))

(defn form-add-people []
  (let [person atom-person]
    (fn []
      [:div
       [rf/bind-fields form-template person]
       [:label (str @person)]])))

(defn form-change-person [p]
    (fn []
      [:div
       [rf/bind-fields form-template p]
       [:label (str @p)]]))

(defn ajax-get-people [people]
  (GET "api/people"
       {:handler #(reset! people (vec %))
        :keywords? true
        :error-handler error-handler}))

(defn ajax-check-person []
  (GET "api/check-person"
       :error-handler error-handler
       :keywords? true
       :format :json
       :params @atom-person))

(defn ajax-save-person []
  (.log js/console (str @atom-person))
    (POST "api/people"
          {:format :json
           :handler handler
           :headers {"Accept" "application/transit+json"
                     "x-forgery-token" (.-value (.getElementById js/document "token"))}
           :params @atom-person
           :keywords? true
           :error-handler error-handler}))

(defn ajax-deleted-person [per_id]
  (DELETE "api/people"
          {:format :json
           :handler handler
           :headers {"x-csrf-token" (.-value (.getElementById js/document "token"))}
           :params @(atom {:per_id per_id})
           :keywords? true
           :error-handler error-handler}))

(defn ajax-save-change [person]
  (PUT "api/people"
       {:format :json
        :handler handler 
        ;:headers {"x-csrf-token" (.-value (.getElementById js/document "token"))}
        :params @person
        :keywords? true
        :error-handler error-handler}))

(defn modal-add-people []
  (#(reagent-modals/modal! [:div
                            [:div {:class "modal-header"}
                             [:h5 {:class "modal-title"} "Add people"]
                             [:button {:type "button" :class "close"
                                       :data-dismiss "modal"
                                       :aria-label "Close"}
                              [:span {:aria-hidden "true"} "×"]]]
                            [:div {:class "modal-body"}
                             [form-add-people]
                             [:div.btn.btn-primary
                              {:on-click ajax-save-person
                               :type :submit }"Save"]]]){:size :lg}))

(defn modal-change-person [person]
  (let [p atom-change-person]
    (reset! p {:per_id (get person "per_id"):name (get person "name") :male (get person "male")
               :dateofb (get person "dateofb") :address (get person "address")
               :policynumber (get person "policynumber")})
    (#(reagent-modals/modal! [:div
                            [:div {:class "modal-header"}
                             [:h5 {:class "modal-title"} "Changed person"]
                             [:button {:type "button" :class "close"
                                       :data-dismiss "modal"
                                       :aria-label "Close"}
                              [:span {:aria-hidden "true"} "×"]]]
                            [:div {:class "modal-body"}
                             [(fn [](form-change-person p))]
                             [:div.btn.btn-primary
                              {:on-click (fn [] (ajax-save-change p))
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
   {:on-click (fn [] (ajax-deleted-person per_id))} "Deleted"])

(defn table-people []
  (let [people atom-people]
    (ajax-get-people people)
    (fn []
      (.log js/console people)
      [:div
       [reagent-modals/modal-window]
       [btn-add-people]
       [:table {:class "table"}
        [:thead
         [:tr
          [:th "#"]
          [:th "NAME"]
          [:th "Male"]
          [:th "Date"]
          [:th "Address"]
          [:th "Polis"]
          [:th "Action"]]]
        [:tbody
         (for [p @people]
           [:tr
            [:th (get p "per_id")]
            [:th (get p "name")]
            [:th (get p "male")]
            [:th (get p "dateofb")]
            [:th (get p "address")]
            [:th (get p "policynumber")]
            [:th
             [btn-change-person p]
             [btn-deleted-person (get p "per_id")]]])]]])))

(defn ^:export run []
  (rdom/render [table-people] (js/document.getElementById "app")))
