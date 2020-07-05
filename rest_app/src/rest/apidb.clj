(ns rest.apidb
    (:require [rest.db :as db]
              [clj-time.coerce :as c]))

(defn transform-date [date]
  (c/to-sql-date (clj-time.core/date-time (get date :year)
                                          (get date :month)
                                          (get date :day))))
(defn get-person-data [body]
  (let [person (update body :dateofb (fn[_](transform-date (body :dateofb))))]
    person))

(defn add-person [body]
  (let [person (get-person-data body)]
    (db/insert :persontest person)))

(defn get-people []
  (db/select :persontest))

(defn update-person [body]
  (let [person (get-person-data body)]
  (db/change :persontest person ["per_id = ?" (person :per_id)])))

(defn remove-person [person-id]
  (db/delete :persontest ["per_id = ?" person-id]))

(defn check-person [body]
  (let [person (get-person-data body)]
    (db/q "persontest" (person :name) (person :male)
          (person :dateofb) (person :address) (person :policynumber))))
