 (set-env!
  :dependencies   '[[org.clojure/clojure "1.7.0" :scope "provided"]
                    [org.immutant/web "2.1.3"]
                    [boot-immutant "0.5.0" :scope "test"]]
  :resource-paths #{"src" "test"})

(require '[boot.immutant :refer :all])

(let [war-opts {:init-fn 'test-app.core/init}]
  (task-options!
    immutant-test war-opts
    immutant-war war-opts))
