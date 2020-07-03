(ns myfirstproject.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as cheshire]
            [rest.core :refer :all]
            [rest.db :as db]
            ;[rest.apidb :as apidb]
            [clj-time.coerce :as c]))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(deftest rest-test
  (testing "Test GET request to /people"
    (let [response (app-routes (-> (mock/request :get "/api/people")))]
          ;body     (parse-body (:body response))]
      (println response)
      (is (= (:status response) 200))))
  (testing "Test POST request to /people"
    (let [person {:name "Pavel"
                  :male "M"
                  :dateofb nil
                  :address "Kairbekova 411"
                  :policynumber "322"}
          response (app-routes (-> (mock/request :post "/api/people")
                            (mock/content-type "application/json")
                            (mock/body (cheshire/generate-string person))))]
                                        ;body     (parse-body (:body response))]
      (println response)
      (is (= (:status response) 200)))))

(deftest db-test
  (is (db/insert :persontest {:name "test3"
                              :male "M"
                              :dateofb (c/to-sql-date  "24-08-1994")
                              :address "test_address"
                              :policynumber "addresss22"}))
  (is (not= () (db/q "persontest" "test3" "M" (c/to-sql-date "24-08-1994") "test_address" "addresss22")))
  (is (not= 0 (db/change :persontest {:name "Pavel"} ["name = ?" "test3"])))
  (is (not= () (db/q "persontest" "Pavel" "M" (c/to-sql-date "24-08-1994") "test_address" "addresss22")))
  (is (db/delete :persontest ["name = ?" "Pavel"])))
