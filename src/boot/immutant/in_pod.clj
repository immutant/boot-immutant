(ns boot.immutant.in-pod
  (:require boot.aether
            [boot.util :as util]
            [cemerick.pomegranate.aether :as aether]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [fntest.core :as fntest]
            [immutant.deploy-tools.war :as war])
  (:import java.io.ByteArrayOutputStream
           java.net.URLClassLoader
           java.util.Properties))

(defn assoc-if-val [m k v]
  (if-not (nil? v) (assoc m k v) m))

(defn war-path [{:keys [destination name target-path]}]
  (.getAbsolutePath
    (io/file (or (war/resolve-target-path destination) target-path)
      (str name ".war"))))

(defn root-dir []
  (System/getProperty "user.dir"))

(def files->paths
  (partial map
    (fn [[k v]]
      [k (if (instance? java.io.File v)
           [:path (.getAbsolutePath v)]
           [:string v])])))

(defn build-war [options]
  (->> (war/create-war-specs
         (assoc options
           :dev? (:dev options)
           :warn-fn (fn [& msg] (util/warn (str (str/join " " msg) "\n")))
           :dependency-resolver #(map io/file (boot.aether/resolve-dependency-jars %))
           :dependency-hierarcher #(aether/dependency-hierarchy (:dependencies %)
                                     (boot.aether/resolve-dependencies* %))
           :root (root-dir)
           :nrepl (-> {:start? (:nrepl-start options)}
                    (assoc-if-val :port (:nrepl-port options))
                    (assoc-if-val :host (:nrepl-host options))
                    (assoc-if-val :port-file (:nrepl-port-file options))
                    (assoc-if-val :options (:nrepl-options options)))))
    files->paths))

(defn run-tests [options]
  (apply
    fntest/test-in-container
    (str "test-project.war")
    (root-dir)
    (-> options
      (assoc
        :jboss-home (:wildfly-home options)
        :modes fntest/default-modes
        :output-fns {:error util/fail
                     :warn  util/warn
                     :info  util/info})
      (cond->
          (:cluster options) (update-in [:modes] conj :domain)
          (:debug options)   (update-in [:modes] conj :debug))
      (->> (mapcat identity)))))

(defn load-app-properties [war-file]
  (doto (Properties.)
    (.load 
      (io/reader (io/resource "META-INF/app.properties"
                   (URLClassLoader. (into-array [(-> war-file .toURI .toURL)])))))))

(defn ensure-nrepl [war-file port-file]
  (let [war-file' (io/file war-file)]
    (with-open [bos (ByteArrayOutputStream.)]
      (-> war-file'
        load-app-properties
        war/properties->partial-options-map
        (merge {:nrepl {:start? true
                        :port-file port-file
                        :host "localhost"
                        :port 0}})
        war/build-descriptor
        war/map->properties
        (.store bos ""))
      (war/replace-file war-file' "META-INF/app.properties" (.toByteArray bos)))))
