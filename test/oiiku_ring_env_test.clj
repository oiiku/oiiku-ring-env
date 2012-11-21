(ns oiiku-ring-env-test
  (:use clojure.test
        oiiku-ring-env))

(def our-ns *ns*)
(def ^:dynamic something-from-outside-ns 123)

(deftest creating-factory-with-all-keys
  (let [all-keys #{:some-env-key :other-env-key}]
    (is (evaluate-env {:some-env-key "foo" :other-env-key "bar"} all-keys))
    (is (thrown? Exception (evaluate-env {:other-env-key "bar"} all-keys)))
    (is (thrown? Exception (evaluate-env {:some-env-key "foo"} all-keys)))
    (is (thrown? Exception (evaluate-env {} all-keys)))
    (is (thrown? Exception (evaluate-env {:cake true} all-keys)))))

(deftest reading-config-files-on-classpath
  (binding [*ns* our-ns]
    (let [env (oiiku-ring-env/read-config-files ["config_file_on_classpath.clj"])]
      (is (= (deref (env :hello)) "from classpath"))
      (is (= (deref (env :testing)) something-from-outside-ns)))))

(deftest reading-config-files-on-file-system
  (binding [*ns* our-ns]
    (let [env (oiiku-ring-env/read-config-files ["fixtures/config_file_in_file_system.clj"])]
      (is (= (deref (env :hello)) "from file system"))
      (is (= (deref (env :testing)) something-from-outside-ns)))))

(deftest merging-config-files
  (binding [*ns* our-ns]
    (let [env-1 (oiiku-ring-env/read-config-files ["config_file_on_classpath.clj"
                                                   "fixtures/config_file_in_file_system.clj"])
          env-2 (oiiku-ring-env/read-config-files ["fixtures/config_file_in_file_system.clj"
                                                   "config_file_on_classpath.clj"])]
      (is (= (deref (env-1 :hello)) "from file system"))
      (is (= (deref (env-1 :testing)) something-from-outside-ns))
      (is (= (deref (env-2 :hello)) "from classpath"))
      (is (= (deref (env-2 :testing)) something-from-outside-ns)))))

(deftest making-lazy-handler
  (binding [*ns* our-ns]
    (let [handler-fn (fn [req] req)
          factory (fn [env]
                    (fn [req] (handler-fn (assoc req :env (evaluate-env env #{:testing})))))
          lazy-handler (oiiku-ring-env/make-lazy-handler
                        ["config_file_on_classpath.clj"]
                        factory)]
      (binding [something-from-outside-ns 456]
        (let [res (lazy-handler {})]
          (is (= (get-in res [:env :testing]) 456)))
        (binding [something-from-outside-ns 789]
          (let [res (lazy-handler {})]
            (is (= (get-in res [:env :testing]) 456))))))))