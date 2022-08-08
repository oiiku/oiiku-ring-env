(ns oiiku-ring-env-test
  (:require [clojure.test :refer :all]
            [oiiku-ring-env :refer :all]))

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
