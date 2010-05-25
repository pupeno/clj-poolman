(ns clj-poolman.core)

(defstruct resource-pool :init :low :high :resources)

(defn next-id
  "Find the next resource id"
  [{:keys [resources]}]
  (let [ids (set (map :id resources))]
    (first (filter #(not (ids %)) (iterate inc 0)))))

(defn new-resource
  "Make a new resource of a pool"
  [{f-init :init :as pool}]
  (let [id (next-id pool)]
    {:id id :resource (f-init)}))

(defn assoc-new-resource
  [{resources :resources :as pool}]
  (assoc pool :resources (conj resources (new-resource pool))))
	 
(defn mk-pool
  "Make a new resource pool where high for high watermark, low for low watermark"
  [high low f-init]
  (let [pool (struct resource-pool f-init low high #{})]
    (reduce (fn [p _] (assoc-new-resource p)) pool (range low))))

(defn get-resource
  [{:keys [init high resources] :as pool}]
  (let [free-resources (filter #(not (:busy %)) resources)
	_ (prn (> high (count resources)))
	resource (if free-resources
		   (first free-resources)
		   (when (> high (count resources))
		     (new-resource pool)))
	resource-after (assoc resource :busy true)
	resources (-> resources (disj resource) (conj resource-after))	
	pool (if resource
	       (assoc pool :resources resources)
	       pool)]
    [pool resource]))
    
(defn release-resource
  [pool resource]
  )