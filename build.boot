(def +version+ "1.0.0-SNAPSHOT")

(task-options!
  pom {:project     'boot-immutant
       :version      +version+
       :description "Boot plugin for managing an Immutant project."
       :url         "https://github.com/immutant/boot-immutant"
       :scm         {:url "https://github.com/immutant/boot-immutant"}
       :license     {"Eclipse Public License - v 1.0"
                     "http://www.eclipse.org/legal/epl-v10.html"}})

(set-env!
  :dependencies   '[[org.clojure/clojure "1.6.0" :scope "provided"]
                    [adzerk/bootlaces "0.1.11" :scope "test"]]
  :resource-paths #{"src" "resources"})

;; see resources/versions.properties for the versions of deploy-tools
;; and fntest used in the pod TODO: move them here and have a task
;; that spits out the props file

(require '[adzerk.bootlaces :refer :all])

(bootlaces! +version+)
