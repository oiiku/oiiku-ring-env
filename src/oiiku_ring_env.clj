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

(defn handler-factory
  [factory-fn & {:keys [all-keys env-key]
                 :or {all-keys []
                      env-key :env}}]
  (let [all-keys (set all-keys)]
    (fn [delayed-env]
      (let [env-keys (set (keys delayed-env))
            missing-keys (clojure.set/difference all-keys env-keys)]
        (if (empty? missing-keys)
          (let [env (deref-env delayed-env all-keys)
                handler (factory-fn env)]
            (fn [req]
              (handler (assoc req env-key env))))
          (throw (Exception. (str "The following config keys were missing: "
                                  missing-keys
                                  ". These are all the required keys: "
                                  all-keys))))))))

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
  (let [real-handler (delay (handler-factory (read-config-files config-files)))]
    (fn [req] ((deref real-handler) req))))