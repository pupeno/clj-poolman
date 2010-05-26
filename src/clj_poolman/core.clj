(ns clj-poolman.core)

(defn next-id
  "Find the next resource id"
  [resources]
  (let [ids (set (map :id resources))]
    (first (filter #(not (ids %)) (iterate inc 0)))))

(defn new-resource
  "Make a new resource of a pool"
  [f-init resources]
  (let [id (next-id resources)]
    {:id id :resource (f-init)}))

(defn assoc-new-resource
  [{:keys [resources init] :as pool}]
  (assoc pool :resources (conj resources (new-resource init resources))))

(defstruct resource-pool :init :close :low :high :resources)
	 
(defn mk-pool
  "Make a new resource pool where high for high watermark, low for low watermark,
   f-init is a function without argument to open a new resource,
   f-close is a function which take resource as a argument and do something to release the resource,
   the return value of f-close will be ignored"
  [high low f-init f-close]
  (let [pool (struct resource-pool f-init f-close low high #{})]
    (reduce (fn [p _] (assoc-new-resource p)) pool (range low))))

(defn get-resource
  [{:keys [init high resources] :as pool}]
  (let [free-resources (filter #(not (:busy %)) resources)
	resource (if (seq free-resources)
		   (first free-resources)
		   (when (> high (count resources))
		     (new-resource init resources)))
	resource-after (assoc resource :busy true)
	resources (-> resources (disj resource) (conj resource-after))	
	pool (if resource
	       (assoc pool :resources resources)
	       pool)]
    [pool resource]))
    
(defn release-resource
  [{:keys [low close resources] :as pool} {res-id :id :as resource}]
  (let [busy-resource (first (filter #(= (:id %) res-id) resources))
	resources (-> resources (disj busy-resource))
	resources (if (>= (count resources) low)
		   (do
		     (when close
		       (close (:resource resource)))
		     resources)
		   (conj resources resource))]
    (assoc pool :resources resources)))