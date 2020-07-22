(ns rest.apidb
  (:require [rest.db :as db]
            [clojure.edn :as edn]
            [clj-time.coerce :as c]
            [clojure.string :as str]))

(defn transform-date [date]
  (c/to-sql-date (clj-time.core/date-time (get date :year)
                                          (get date :month)
                                          (get date :day))))

(transform-date {:year 2005 :month 05 :day 22})


(defn transform-output-date [date]
  (let [str-date (str date)]
    (zipmap [:year :month :day] (map edn/read-string (str/split
                                                      (first(str/split str-date #"T"))
                                                      #"-0?")))))

;(get-people)
(def test-date [{:dateofb "2005-05-22-" :name "pavel"}
                {:dateofb "1994-22-05-"} :name "pavek"])

(def test-date2 [{:dateofb "2005-05-23"}
                 {:dateofb "1994-22-22"}])

;(transform-output-list [{:foo 1 :dateofb "2020-07-05T19:00:00Z"}
;                        {:foo 2 :dateofb "2001-05-22T18:00:00Z"}])

(def d  "test")

(map (fn [x] (update x :bar str))
     [{:foo 1 :bar 2}
      {:foo 2 :bar 3}])

[(vals {:y 2000 :m 20 :d 22})]



(defn transform-output-list [list-people]
  (map (fn [x] (update x :dateofb transform-output-date))
       list-people))

(defn get-person-data [body]
  (let [person (update body :dateofb (fn[_](transform-date (body :dateofb))))]
    person))

(defn add-person [body]
  (let [person (get-person-data body)]
    (db/insert :persontest person)))

(defn get-people []
  (let [people (db/select :persontest)
        trans-people (transform-output-list people)]
    trans-people))


(defn update-person [body]
  (let [person (get-person-data body)]
  (db/change :persontest person ["per_id = ?" (person :per_id)])))

(defn remove-person [person-id]
  (db/delete :persontest ["per_id = ?" person-id]))

(defn check-person [body]
  (let [person (get-person-data body)]
    (db/q "persontest" (person :name) (person :male)
          (person :dateofb) (person :address) (person :policynumber))))
