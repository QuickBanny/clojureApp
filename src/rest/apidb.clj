(ns rest.apidb
  (:require [rest.db :as db]))

(defn add-person [{name :name male :male dateofb :dateofb address :address policynumber :policynumber :as record}]
  (db/insert :persontest record))

(defn get-people []
  (db/select :persontest))

(defn update-person [{name :name male :male dateofb :dateofb address :address policynumber :policynumber :as update}]
  (db/change :persontest update))

(defn remove-person [id]
  (db/delete :persontest ["id_per = ?" id]))

(defn get-person [name male dateofb address policynumber]
  (db/q (name :persontest) name male dateofb address policynumber))
