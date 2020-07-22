(ns rest-app.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as cheshire]
            [rest.core :refer :all]
            [rest.db :as db]
            [rest.apidb :as apidb]
            [clj-time.coerce :as c]
            [clojure.data.json :as json]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]))


(def atom-person {:name "Pavel2"
                   :male "M"
                   :dateofb {:year 1994 :month 05 :day 22}
                   :address "Kairbekova"
                   :policynumber "12345"})

(def atom-person-empty {:name ""})

(def map-date {:year 2005 :month 05 :day 22})

(def list-output-data (vector{:name "Pavel" :dateofb "2020-07-05T19:00:00Z"}
                             {:name "Pavel22" :dateofb "1994-05-22T18:00:44Z"}))

(def list-output-data-res [{:name "Pavel" :dateofb {:year 2020 :month 07 :day 19}}
                           :name "Pavel22" :dateofb {:year 1994 :month 05 :day 22}])

(deftest tr-data
  (testing "Transform output data"
      (is (= (apidb/transform-output-list list-output-data) list-output-data-res))))

(deftest rest-test
  (testing "Test GET request to /api/people"
    (let [response (app-routes (-> (mock/request :get "/api/people")))]
      (is (= (:status response) 200))))
  (testing "Test POST request to /people"
    (let [handler (-> app-routes (wrap-json-body {:keywords? true :bigdecimals? true}))
          json-body (cheshire/generate-string @atom-person)
          request (-> (mock/request :post "/api/people")
                      (mock/body json-body)
                      (mock/content-type "application/json"))
          response (handler request)
          body     (json/read-str (:body response) :key-fn keyword)
          insert-person-id (body :per_id)]
      (is (= (:status response) 200))
      (let [handler (-> app-routes (wrap-json-body {:keywords? true :bigdecimals? true}))
            json-body (cheshire/generate-string @(atom {:per_id insert-person-id}))
            request (-> (mock/request :delete "/api/people")
                        (mock/body json-body)
                        (mock/content-type "application/json"))
            response (handler request)]
        (is (= (:status response) 200)))))
  (testing "Test POST empty person to /people"
    (let [handler (-> app-routes (wrap-json-body {:keywords? true :bigdecimals? true}))
          json-body (cheshire/generate-string @atom-person-empty)
          request (-> (mock/request :post "/api/people")
                      (mock/body json-body)
                      (mock/content-type "application/json"))
          response (handler request)]
      (is (= (:status response) 400)))))

(deftest db-test
  (testing "Test good insert, check and remove in DB"
  (let [insert-person @atom-person]
    (is (not-empty (apidb/add-person insert-person)))
    (let [person-db (apidb/check-person insert-person)]
      (is (not-empty person-db))
      (is (not-empty (apidb/remove-person (get (first person-db) :per_id))))))))
