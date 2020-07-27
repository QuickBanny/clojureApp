(ns rest.subs
  (:require
   [re-frame.core :as rf]))

(rf/reg-sub
 :people-list
 (fn [db _]
   (:people-list db)))
