(ns rest.util
  (:require
   [clj-time.coerce :as c]
   [clojure.string :as str]
   [clojure.edn :as edn]))

(defn generate-map [data]
  (let [d data]
    (into {} (map (fn [[k v]] [(keyword k) v])
                  d))))

(defn contains-many? [m & ks]
  (every? #(contains? m %) ks))

(defn transform-date [date]
  (let [date (generate-map date)]
    (c/to-sql-date (clj-time.core/date-time (:year date)
                                            (:month date)
                                            (:day date)))))

(defn transform-output-date [date]
  (let [str-date (str date)]
    (zipmap [:year :month :day] (map edn/read-string (str/split
                                                      (first(str/split str-date #"T"))
                                                      #"-0?")))))

(defn transform-output-list [list-people]
  (into [] (map (fn [x] (update x :dateofb transform-output-date))
                list-people)))

(defn map-to-vector [map]
  (persistent!
   (reduce
    (fn [acc0 item-vector]
      (reduce
       (fn [acc1 item]
         (conj! acc1 item))
       acc0 item-vector))
    (transient [])
    (into [] map))))

(defn tr-and-where-sql [person-map]
  (into [:and] (map (fn [[k v]] [:= k v])
                    person-map)))

(defn tr-where-sql [person-map]
  (map (fn [[k v]] [:= k v])
       person-map))


