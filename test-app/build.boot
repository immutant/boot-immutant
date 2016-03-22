 (set-env!
  :dependencies '[[org.clojure/clojure "1.7.0" :scope "provided"]
                  [org.immutant/web "2.1.3"]
                  [boot-immutant "0.6.0-SNAPSHOT" :scope "test"]]
  :source-paths #{"src" "test"})

(require '[boot.immutant :refer :all])

(deftask build-war []
  (comp
    (uber :as-jars true)
    (aot :all true)
    ;; (sift :to-resource [#".*"]) ;; or this, if you don't want aot
    (gird :init-fn 'test-app.core/init)
    (war :file "foo.war")
    (target)
    ))

(deftask build-dev-war []
  (comp
    (gird :dev true :init-fn 'test-app.core/init)
    (war)
    (target)))

(deftask run-tests []
  (comp
    (build-dev-war)
    (test-in-container)))
