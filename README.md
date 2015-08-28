# clj-poolman

A general purpose resource pool for Clojure.
You can use it to manage database connection, file handles etc.

## Usage

To make a new resource pool that provides resources,

```clojure
(defn make-db-connection
  []
  (connect-db "localhost" 3223))

(defn close-db-connection
  [connection]
  (.close connection))
  
(def db-pool (mk-pool 5 3 make-db-connection close-db-connection))

;;5 for high water mark of the pool, 3 for low water mark of the pool
```

The pool gurantees resources of low water mark, which is 3 in this case,
and if at the peak time resource requirement higher than low water mark, the
pool will try to make more resources (temporary resources) up to the high
water mark. Once a temporary resource returns to the pool, it will be released.

If the resources reach the high water mark already, subsequent resource
requirement will return nil.

You use with-resource to grab a resource and use it:

```clojure
(with-resource [conn db-pool]
  (query conn "select ok!"))
```

To shutdown a resource pool,

```clojure
(shutdown-pool db-pool)
```
