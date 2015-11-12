(set-env!
  :dependencies '[[org.clojure/clojure "1.7.0" :scope "provided"]
                  [adzerk/bootlaces "0.1.11" :scope "test"]]
  :resource-paths #{"src" "resources"})

(require
  '[boot.util :as util]
  '[adzerk.bootlaces :refer :all :exclude [build-jar] :as laces]
  '[clojure.java.io :as io])

(def +version+ "0.5.0-SNAPSHOT")

(bootlaces! +version+)

(task-options!
  pom {:project     'boot-immutant
       :version      +version+
       :description "Boot plugin for managing an Immutant project."
       :url         "https://github.com/immutant/boot-immutant"
       :scm         {:url "https://github.com/immutant/boot-immutant"}
       :license     {"Eclipse Public License - v 1.0"
                     "http://www.eclipse.org/legal/epl-v10.html"}})

(def in-pod-dependencies
  [['boot-immutant             +version+]
   ['org.immutant/deploy-tools "2.1.0"]
   ['org.immutant/fntest       "2.0.10"]])

(deftask write-pod-dependencies []
  (with-pre-wrap fileset
    (let [tgt (temp-dir!)]
      (util/info "Writing in-pod-dependencies.edn...\n")
      (spit (io/file tgt "in-pod-dependencies.edn")
        (pr-str in-pod-dependencies))
      (-> fileset (add-resource tgt) commit!))))

(deftask build-jar [] (comp (write-pod-dependencies) (laces/build-jar)))

(deftask release [] (comp (write-pod-dependencies) (pom) (jar) (push-release)))
