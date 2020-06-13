(ns myfirstproject.core-test
  (:require [clojure.test :refer :all]
            [rest.core :refer :all]
            [rest.db :as db]))

(deftest db-test
  (is (db/insert-person "test" "M" "24-08-1994" "test_address" "address"))
  (is (not= () (db/check-person "test" "M" "24-08-1994" "test_address" "address")))
  (is (not= 0 (db/update-person {:name "Pavel"} ["name = ?" "test"])))
  (is (not= () (db/check-person "Pavel" "M" "24-08-1994" "test_address" "address")))
  (is (db/remove-person["name = ?" "Pavel"])))

