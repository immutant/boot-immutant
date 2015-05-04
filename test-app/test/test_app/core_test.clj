(ns test-app.core-test
  (:require [test-app.core :refer :all]
            [clojure.test :refer :all]))

(deftest a-passing-test
  (is (= "whatever" (whatever))))
