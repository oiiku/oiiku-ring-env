(ns oiiku-ring-env
  (:require clojure.set))

(defn- realize-env
  "Takes a env that might contain unevaled values and creates a new map with
   all the keys in `keys` with evaled values."
  [unevaled-env all-keys]
  (reduce #(assoc %1 %2 (eval (unevaled-env %2))) {} all-keys))

(defn handler-factory
  [factory & {:keys [all-keys env-key]
      :or {all-keys []
           env-key :env}}]
  (let [all-keys (set all-keys)]
    (fn [unrealized-env]
      (let [env-keys (set (keys unrealized-env))
            missing-keys (clojure.set/difference all-keys env-keys)]
        (if (empty? missing-keys)
          (let [realized-env (realize-env unrealized-env all-keys)
                handler (factory realized-env)]
            (fn [req]
              (handler (assoc req env-key realized-env))))
          (throw (Exception. (str "The following config keys were missing: "
                                  missing-keys
                                  ". These are all the required keys: "
                                  all-keys))))))))

(defn read-config-files
  "Reads a list of files from classpath or file system and merges them from
   left to right."
  [files]
  (apply merge
         (map (fn [file]
                (-> (or (clojure.java.io/resource file) file)
                    slurp
                    read-string))
              files)))

(defn make-lazy-handler
  [config-files handler-factory]
  (let [real-handler (delay (handler-factory (read-config-files config-files)))]
    (fn [req] ((deref real-handler) req))))