(ns rest.events
  (:require
   [re-frame.core :as rf]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   [clojure.string :as str]))

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:people-list []}))

(def api-url "http://127.0.0.1:3000/api")

(defn endpoint[& params]
  (str/join "/" (concat [api-url] params)))

(rf/reg-event-fx
 :get-people
 (fn [{:keys [db]} [_ _]]
   {:http-xhrio {:method :get
                 :uri (endpoint "people")
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:get-people-success]
                 :on-failure [:get-people-failure]}
    }))

(rf/reg-event-db
 :get-people-success
 (fn [db [_ people-list]]
   (.log js.console (str people-list))
   (assoc db :people-list people-list)))

(rf/reg-event-fx
 :upsert-people
 (fn [{:keys [db]} [_ params]]
   {:http-xhrio {:method (if (:per_id params) :put :post)
                 :uri (endpoint "people")
                 :params params
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:upsert-people-success]
                 :on-failure [:api-request-error]
                 }}))

(rf/reg-event-fx
 :upsert-people-success
 (fn [{:keys [db]} [_]]
   {:db db
    :dispatch [:get-people]}))

(rf/reg-event-fx
 :delete-people
 (fn [{:keys [db]} [_ per_id]]
   {:http-xhrio {:method :delete
                 :uri (endpoint "people")
                 :params {:per_id per_id}
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:delete-people-success]
                 :on-failure [:api-request-error]}}))

(rf/reg-event-fx
 :delete-people-success
 (fn [{:keys [db]} [_]]
   {:db db
    :dispatch [:get-people]
    }))
