(ns boot.immutant
  (:require [boot.core :as boot :refer [deftask]]
            [boot.pod :as pod]
            [boot.util :as util]
            [boot.task.built-in :as built-in]
            [boot.tmpdir :as tmpd]
            [boot.from.digest :as digest]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [boot.from.backtick :as bt])
  (:import java.util.Properties))

(defn ^:private load-data [resource-name]
  (-> resource-name io/resource slurp read-string))

(defn ^:private in-pod-dependencies []
  (conj (load-data "in-pod-dependencies.edn") ['boot/aether boot/*boot-version*]))

(def ^:private pod
  (delay
    (pod/make-pod
      (assoc pod/env :dependencies (in-pod-dependencies)))))

(defn ^:private gen-classpath
  [{:keys [source-paths resource-paths] :as env}]
  (into (mapv #(-> % io/file .getAbsolutePath)
          (concat source-paths resource-paths))
    (pod/with-call-worker
      (boot.aether/resolve-dependency-jars ~env))))

(defn ^:private print-guide [guide]
  (-> guide name (str "-guide.md") io/resource slurp println))

(defn ^:private ensure-dir [f]
  (.mkdirs (.getParentFile f))
  f)

(defn ^:private ensure-wildfly-home [wf-home-option]
  (if-let [home (or wf-home-option (System/getenv "WILDFLY_HOME"))]
    (if (.exists (io/file home))
      home
      (throw (Exception. (format "WildFly home '%s' does not exist." home))))
    (throw (Exception. "No WildFly home specified. Specify via --wildfly-home or $WILDFLY_HOME."))))

(defn ^:private split-path [path]
  (let [parts (str/split path #"/")]
    [(str/join "/" (butlast parts)) (last parts)]))

(defn ^:private hashed-name [name file]
  (str (digest/md5 file) "-" name))

(defn ^:private war-machine [fileset {:keys [init-fn name nrepl-start nrepl-port-file dev] :as opts}]
  (when-not init-fn
    (throw (Exception. "No :init-fn specified!")))
  (let [env (boot/get-env)
        tmp (boot/tmp-dir!)
        res (pod/call-in* @pod
              ['boot.immutant.in-pod/build-war
               (-> env
                 (select-keys [:dependencies :repositories :local-repo :offline? :mirrors :proxy :target-path])
                 (merge opts)
                 (assoc
                   :nrepl-port-file (when nrepl-port-file (.getAbsolutePath nrepl-port-file))
                   :name        (or name "project")
                   :nrepl-start (if (contains? opts :nrepl-start) nrepl-start dev)
                   :classpath   (when dev (gen-classpath env))))])
        existing-output (boot/output-files fileset)]
    (doseq [[path [type in]]
            res]
      (let [file? (= type :path)
            in' (if file? (io/file in) in)
            [path-prefix name] (split-path path)
            name' (if (and file? (.endsWith name ".jar"))
                    (hashed-name name in')
                    name)]
        (when-not (some #(= name' (tmpd/path %)) existing-output)
          (io/copy in' (ensure-dir (io/file tmp path-prefix name'))))))
    (-> fileset (boot/add-resource tmp) boot/commit!)))

(deftask immutant-war
  "Generates a war file suitable for deploying to a WildFly container.

   For detailed help, see `boot immutant-war --guide`."
  [_ guide                bool  "Display the usage guide. All other options ignored [false]"
   i init-fn         FN   sym   "The 'main' function to call on deploy [nil]"
   d dev                  bool  "Generate a 'dev' war [false]"
   c context-path    PATH str   "Deploy to this context path [nil]"
   v virtual-host    HOST [str] "Deploy to the named host defined in the WildFly config [nil]"
   r resource-path   PATH [str] "Paths to file trees to include in the top level of the war [nil]"
   _ nrepl-start          bool  "Request nrepl to start [dev]"
   _ nrepl-host      HOST str   "Host for nrepl to bind to [\"localhost\"]"
   _ nrepl-port      PORT int   "Port for nrepl to bind to [0]"
   _ nrepl-port-file FILE file  "File to write actual nrepl port to [nil]"
   _ nrepl-options   CODE code  "Repl options map [{}]"]
  (boot/with-pre-wrap fileset
    (if guide
      (do
        (print-guide :deployment)
        fileset)
      (war-machine fileset *opts*))))

(deftask immutant-test
  "Runs a project's tests inside WildFly.

   For detailed help, see `boot immutant-test --guide`."
  [_ guide              bool  "Display the usage guide. All other options ignored [false]"
   c cluster            bool  "Deploy the test application to a cluster [false]"
   d debug              bool  "Start the server with debugging enabled [false]"
   w wildfly-home  PATH str   "Use the WildFly at PATH [(System/getenv \"WILDFLY_HOME\")]"
   o port-offset   AMT  int   "Offset the WildFly network ports [67]"
   i init-fn       FN   sym   "The 'main' function to call on deploy [nil]"
   r resource-path PATH [str] "Paths to file trees to include in the top level of the war [nil]"]
  ;; TODO: support test selection? log level?
  (let [tmp (boot/tmp-dir!)
        war-name "project-test"
        war-file (io/file tmp (str war-name ".war"))]
    (boot/with-pre-wrap fileset
      (if guide
        (print-guide :testing)
        (let [wildfly-home' (ensure-wildfly-home wildfly-home)
              port-file (io/file tmp "nrepl-port")
              {:keys [resource-paths source-paths target-path]} (boot/get-env)
              isolation-dir (str target-path "/isolated-wildfly")]
          (war-machine
            (assoc (select-keys *opts* [:init-fn :resource-path])
              :dev true
              :nrepl-start true
              :nrepl-port-file port-file
              :destination (.getAbsolutePath tmp)
              :name war-name))
          (util/info
            (format
              "Running tests inside WildFly (log output available in %s/isolated-wildfly/%s/log/server.log)...\n"
              target-path
              (if cluster "domain/servers/*" "standalone")))
          (when-not
              (pod/call-in* @pod
                ['boot.immutant.in-pod/run-tests
                 (assoc *opts*
                   :isolation-dir isolation-dir
                   :dirs (into resource-paths source-paths)
                   :port-file (.getAbsolutePath port-file)
                   :war-file (.getAbsolutePath war-file)
                   :wildfly-home wildfly-home')])
            (throw (Exception. "Tests failed or errored.")))))
      fileset)))
