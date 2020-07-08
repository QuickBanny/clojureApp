(ns rest.apidb
  (:require [rest.db :as db]
            [clojure.edn :as edn]
            [clj-time.coerce :as c]
            [clojure.string :as str]))

(defn transform-date [date]
  (c/to-sql-date (clj-time.core/date-time (get date :year)
                                          (get date :month)
                                          (get date :day))))
(defn transform-output-date [date]
  (zipmap [:year :month :day] (map edn/read-string (str/split date #"-0?"))))

(defn get-person-data [body]
  (let [person (update body :dateofb (fn[_](transform-date (body :dateofb))))]
    person))

(defn output-person [person-db]
  (let [person (update person-db :dateofb
                       (fn[_](transform-output-date (person-db :dateofb))))]
    person))

(defn add-person [body]
  (let [person (get-person-data body)]
    (db/insert :persontest person)))

(defn get-people []
  (let [people (db/select :persontest)]
    people))

(defn update-person [body]
  (let [person (get-person-data body)]
  (db/change :persontest person ["per_id = ?" (person :per_id)])))

(defn remove-person [person-id]
  (db/delete :persontest ["per_id = ?" person-id]))

(defn check-person [body]
  (let [person (get-person-data body)]
    (db/q "persontest" (person :name) (person :male)
          (person :dateofb) (person :address) (person :policynumber))))
