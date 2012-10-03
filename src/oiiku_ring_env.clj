(ns oiiku-ring-env
  (:require clojure.set))

(defn handler-factory
  [factory & {:keys [required-keys env-key env-processor]
      :or {required-keys []
           env-key :env
           env-processor (fn [env] env)}}]
  (let [required-keys (set required-keys)]
    (fn [env]
      (let [env (env-processor env)
            env-keys (set (keys env))
            missing-keys (clojure.set/difference required-keys env-keys)]
        (if (empty? missing-keys)
          (let [handler (factory env)]
            (fn [req]
              (handler (assoc req env-key env))))
          (throw (Exception. (str "The following config keys were missing: "
                                  missing-keys
                                  ". These are all the required keys: "
                                  required-keys))))))))

(defn read-config-files
  "Reads a list of files from classpath or file system and merges them from
   left to right."
  [files]
  (apply merge
         (map (fn [file]
                (-> (or (clojure.java.io/resource file) file)
                    slurp
                    read-string
                    eval))
              files)))

(defn make-lazy-handler
  [config-files handler-factory]
  (let [real-handler (delay (handler-factory (read-config-files config-files)))]
    (fn [req] ((deref real-handler) req))))