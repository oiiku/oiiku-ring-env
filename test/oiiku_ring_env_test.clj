(ns oiiku-ring-env-test
  (:require oiiku-ring-env)
  (:use clojure.test))

(def our-ns *ns*)
(def ^:dynamic something-from-outside-ns 123)

(deftest creating-factory
  (let [factory (fn [env] (fn [req] (assoc req :handled "foo")))
        factory (oiiku-ring-env/handler-factory
                 factory
                 :all-keys [:some-env-key])
        mock-env {:some-env-key 123}
        handler (factory mock-env)
        mock-req {:some-req-key "test"}
        res (handler mock-req)]
    (is (= res
           {:handled "foo" :some-req-key "test" :env {:some-env-key 123}}))))

(deftest creating-factory-with-all-keys
  (let [factory (oiiku-ring-env/handler-factory
                 (fn [env] (fn [req]))
                 :all-keys [:some-env-key :other-env-key])]
    (is (factory {:some-env-key "foo" :other-env-key "bar"}))
    (is (thrown? Exception (factory {:other-env-key "bar"})))
    (is (thrown? Exception (factory {:some-env-key "foo"})))
    (is (thrown? Exception (factory {})))
    (is (thrown? Exception (factory {:cake true})))))

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
    (let [factory (oiiku-ring-env/handler-factory
                   (fn [env] (fn [req] req))
                   :all-keys [:testing])
          lazy-handler (oiiku-ring-env/make-lazy-handler
                        ["config_file_on_classpath.clj"]
                        factory)]
      (binding [something-from-outside-ns 456]
        (let [res (lazy-handler {})]
          (is (= (get-in res [:env :testing]) 456)))
        (binding [something-from-outside-ns 789]
          (let [res (lazy-handler {})]
            (is (= (get-in res [:env :testing]) 456))))))))