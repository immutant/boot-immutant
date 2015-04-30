 (set-env!
  :dependencies   '[[org.clojure/clojure "1.6.0" :scope "provided"]
                    [org.immutant/web "2.0.0"]
                    [boot-immutant "0.1.0" :scope "test"]]
  :resource-paths #{"src"})

(require '[boot.immutant :refer :all])
