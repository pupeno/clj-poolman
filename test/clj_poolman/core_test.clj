(ns clj-poolman.core-test
  (:use [clj-poolman.core] :reload-all)
  (:use [clojure.test]))

(deftest finding-next-id
  (are [res expected] (= (next-id res) expected)
       nil 0
       #{{:id 0} {:id 1} {:id 2}} 3
       #{{:id 0} {:id 4}} 1))

(deftest initing
  (let [init (fn [] "ok")]
    (is (= (mk-pool* 5 3 init nil)
	 {:resources #{{:id 2 :resource "ok"} {:id 1 :resource "ok"} {:id 0 :resource "ok"}}
	  :high 5 :low 3 :init init :close nil}))))

(deftest getting-resource
  (let [init (fn [] "ok")]
    (are [pool expected] (= (get-resource pool) expected)
	 {:resources #{{:id 0 :resource "ok"}} :high 2}
	 [{:resources #{{:id 0 :resource "ok" :busy true}} :high 2} {:id 0 :resource "ok"}]
	 {:resources #{{:id 0 :resource "ok" :busy true}} :high 2 :init init}
	 [{:resources #{{:id 0 :resource "ok" :busy true} {:id 1 :resource "ok" :busy true}} :high 2 :init init}
	  {:id 1 :resource "ok"}]
	 {:resources #{{:id 0 :resource "ok" :busy true}} :high 1}
	 [{:resources #{{:id 0 :resource "ok" :busy true}} :high 1} nil])))

(deftest releasing
  (are [pool expected] (= (release-resource pool {:id 0, :resource "ok"}) expected)
       {:resources #{{:id 0 :resource "ok" :busy true}} :low 1}
       {:resources #{{:id 0 :resource "ok"}} :low 1}
       {:resources #{{:id 0 :resource "ok" :busy true}} :low 0}
       {:resources #{} :low 0}))
       
       
