(ns boot.immutant
  (:require [boot.core :as boot :refer [deftask]]
            [boot.pod :as pod]
            [boot.util :as util]
            [boot.task.built-in :as built-in]
            [boot.tmpdir :as tmpd]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [boot.from.backtick :as bt])
  (:import java.util.Properties))

(defn ^:private load-properties [resource-name]
  (doto (Properties.)
    (.load (-> resource-name io/resource io/reader))))

(def ^:private versions
  (delay
    (let [version-props (load-properties "versions.properties")
          pom-props (load-properties "META-INF/maven/boot-immutant/boot-immutant/pom.properties")]
      (into
        {:boot-immutant (.getProperty pom-props "version")}
        (for [k (.stringPropertyNames version-props)]
          [(keyword k) (.getProperty version-props k)])))))


(def ^:private pod
  (delay
    (pod/make-pod (assoc pod/env
                    :dependencies [['org.immutant/deploy-tools
                                    (:deploy-tools @versions)]
                                   ['boot-immutant (:boot-immutant @versions)]
                                   ['boot/aether boot/*boot-version*]]))))

(defn ^:private gen-classpath
  [{:keys [source-paths resource-paths] :as env}]
  (into (mapv #(-> % io/file .getAbsolutePath)
          (concat source-paths resource-paths))
    (pod/with-call-worker
      (boot.aether/resolve-dependency-jars ~env))))

(defn ^:private gen-uberjar []
  ;; TODO: it would be nice if the jar task took an absolute path
  (let [fname "project-uber.jar"]
    (boot/boot (built-in/uber) (built-in/jar :file fname))
    (.getAbsolutePath
      (doto (io/file (str "target/" fname))
        (.deleteOnExit)))))

(deftask immutant-war
  "Creates an Immutant war."
  [i init-fn         FN   sym   "The 'main' function to call on deploy [nil]"
   d dev                  bool  "Generate a 'dev' war [false]"
   c context-path    PATH str   "Deploy to this context path [nil]"
   v virtual-host    HOST [str] "Deploy to the named host defined in the WildFly config [nil]"
   o destination     DIR  str   "Write the generated war to DIR [\"./target\"]"
   n name            NAME str   "Override the name of the war (sans the .war suffix) [\"project\"]"
   r resource-path   PATH [str] "Paths to file trees to include in the top level of the war [nil]"
   _ nrepl-host      HOST str   "Host for nrepl to bind to [\"localhost\"]"
   _ nrepl-port      PORT int   "Port for nrepl to bind to [0]"
   _ nrepl-port-file FILE file  "File to write actual nrepl port to [nil]"
   _ nrepl-start          bool  "Request nrepl to start [dev]"
   _ nrepl-options   CODE code  "Repl options map [{}] (needs docs)" ;; TODO: make this actually work
   ]
  (boot/with-pre-wrap fileset
    (when-not init-fn
      (util/warn "No :init-fn specified, no app initialization will be performed in-container.\n"))
    (let [env (boot/get-env)
          war-path (pod/call-in* @pod
                     ['boot.immutant.in-pod/war-machine
                      (-> env
                        (select-keys [:dependencies :repositories :local-repo :offline? :mirrors :proxy])
                        (merge *opts*)
                        (assoc
                          :name (or name "project")
                          :nrepl-start (if (contains? *opts* :nrepl-start) nrepl-start dev)
                          :classpath (when dev (gen-classpath env))
                          :uberjar (when-not dev (gen-uberjar))))])]
      (util/info
        (format "Immutant war written to %s\n" war-path)))
    fileset))
