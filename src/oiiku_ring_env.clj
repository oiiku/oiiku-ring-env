(ns oiiku-ring-env
  (:require clojure.set))

(defn- deref-env
  "Takes a env that might contain unevaled values and creates a new map with
   all the keys in `keys` with evaled values."
  [unevaled-env all-keys]
  (reduce (fn [res key]
            (let [value (unevaled-env key)]
              (assoc res key (if (delay? value) (deref value) value))))
          {}
          all-keys))

(defn evaluate-env
  "An env is just a map, but its values may contain delayed values for lazy
   loading in case of function calls etc. This function takes a full env and
   a set of keys and returns a map containing only the keys in all-keys and
   makes sure its values are evaluated."
  [delayed-env all-keys]
  (let [provided-keys (set (keys delayed-env))
        missing-keys (clojure.set/difference all-keys provided-keys)]
    (if (empty? missing-keys)
      (deref-env delayed-env all-keys)
      (throw (Exception. (str "The following config keys were missing: "
                              missing-keys
                              ". Required keys: " all-keys
                              ". Provided keys: " provided-keys))))))

(defn read-config-files
  "Reads a list of files from classpath or file system and merges them from
   left to right.

   The values are delays so that the values won't be read until actually
   read out in handler-factory."
  [files]
  (apply merge
         (map
          (fn [file]
            (let [unevaled (-> (or (clojure.java.io/resource file) file)
                               slurp
                               read-string)]
              (zipmap (keys unevaled)
                      (map #(delay (eval %)) (vals unevaled)))))
          files)))

(defn make-lazy-handler
  [config-files handler-factory]
  (let [lazy-handler (ref nil)]
    (fn [req]
      (if-let [handler @lazy-handler]
        (handler req)
        (let [new-handler (handler-factory (read-config-files config-files))]
          (dosync
           (ref-set lazy-handler new-handler))
          (new-handler req))))))

(defn make-lazy-env
  "Make only a lazy env, no handler, that when dereferenced returns the env."
  [config-files required-keys]
  (delay
   (oiiku-ring-env/evaluate-env
    (oiiku-ring-env/read-config-files config-files)
    required-keys)))