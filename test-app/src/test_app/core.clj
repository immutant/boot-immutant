(ns test-app.core
  (:require [immutant.web :as web]))

(defn whatever []
  "whatever")

(defn init []
  (println "INIT CALLED")
  (web/run (fn [_] {:status 200 :body "howdy"})))


