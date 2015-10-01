 (set-env!
  :dependencies   '[[org.clojure/clojure "1.6.0" :scope "provided"]
                    [org.immutant/web "2.1.0"]
                    [boot-immutant "0.4.0" :scope "test"]]
  :resource-paths #{"src" "test"})

(require '[boot.immutant :refer :all])
