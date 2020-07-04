(ns rest.apidb
    (:require [rest.db :as db]
              [clj-time.coerce :as c]))


(defn transform-date [date]
  (println date))

(defn add-person [{name :name male :male dateofb :dateofb address :address policynumber :policynumber :as record}]
  (db/insert :persontest record))

(defn get-people []
  (db/select :persontest))

(defn update-person [{per_id :per_id name :name male :male dateofb :dateofb address :address policynumber :policynumber :as update}]
  (db/change :persontest update ["per_id = ?" (get update "per_id")]))

(defn remove-person [id]
  (db/delete :persontest ["per_id = ?" id]))

(defn check-person [{per_id :per_id name :name male :male dateofb :dateofb address :address policynumber :policynumber :as person}]
  (db/q "persontest" (get person "name")
        (get person "male") (get person "dateofb")
        (get person "address") (get person "policynumber")))
