 (set-env!
  :dependencies   '[[org.clojure/clojure "1.7.0" :scope "provided"]
                    [org.immutant/web "2.1.3"]
                    [boot-immutant "0.6.0-SNAPSHOT" :scope "test"]]
  :resource-paths #{"src" "test"})

(require '[boot.immutant :refer :all])

(let [war-opts {:init-fn 'test-app.core/init}]
  (task-options!
    immutant-test war-opts
    immutant-war war-opts))

(deftask build-war []
  (comp
    (uber :as-jars true)
    (immutant-war)
    (war)
    (target)))
