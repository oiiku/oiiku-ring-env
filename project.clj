(defproject oiiku-ring-env "0.2.0-SNAPSHOT"
  :description "Multi-environment ring handlers"
  :license {:name "New BSD license"}
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :profiles {:test {:resource-paths ["test/resources"]}}
  :deploy-repositories
  {"releases" {:url "http://augustl.com:8081/nexus/content/repositories/releases"}
   "snapshots" {:url "http://augustl.com:8081/nexus/content/repositories/snapshots"}})
