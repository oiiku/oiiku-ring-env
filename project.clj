(defproject oiiku-ring-env "0.4.0-SNAPSHOT"
  :description "Multi-environment ring handlers"
  :url "https://github.com/oiiku/oiiku-ring-env"
  :license {:name "New BSD license"}
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :profiles {:test {:resource-paths ["test/resources"]}}
  :repositories
  {"oiiku-releases" "https://nexus.oiiku.no/nexus/content/repositories/releases"
   "oiiku-snapshots" "https://nexus.oiiku.no/nexus/content/repositories/snapshots"}
  :deploy-repositories
  {"releases" {:url "https://nexus.oiiku.no/nexus/content/repositories/releases"}
   "snapshots" {:url "https://nexus.oiiku.no/nexus/content/repositories/snapshots"}})
