(ns rest.apidb
  (:require [rest.db :as db]
            [rest.util :refer :all]))


;; ;(get-people)
;; (def test-date [{:dateofb "2005-05-22-" :name "pavel"}
;;                 {:dateofb "1994-22-05-"} :name "pavek"])

;; (def test-date2 [{:dateofb "2005-05-23"}
;;                  {:dateofb "1994-22-22"}])

;(transform-output-list [{:foo 1 :dateofb "2020-07-05T19:00:00Z"}
;                        {:foo 2 :dateofb "2001-05-22T18:00:00Z"}])

(defn get-person-data [body]
  (let [person (update body :dateofb (fn[_](transform-date (body :dateofb))))]
    person))

(defn add-person [body]
  (let [person (get-person-data body)]
    (db/insert :persontest [person])))

(defn get-people []
  (let [people (db/get-all-person [:persontest])
        trans-people (transform-output-list people)]
    trans-people))

(defn update-person [body]
  (let [person (get-person-data body)]
    (db/change :persontest person)))

(defn remove-person [body]
  (db/mdelete :persontest body))

(defn check-person [body]
  (let [person (get-person-data body)]
    (db/get-person [:per_id] [:persontest] person)))
