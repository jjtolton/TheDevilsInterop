(ns metal-interop.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [org.httpkit.client :as client]
            [tech.v3.datatype.ffi :as dt-ffi]
            [clojure.tools.logging :as log]
            [metal-interop.util :as utils]
            [camel-snake-kebab.core :as csk]
            
            ;; reserved for future \m/ -- zero-copy data
            
            ;; libpython-clj2.python.np-array
            ;; [libpython-clj2.python :as python]
            ))

(defonce return-buffer (atom nil))

(defn clear-return-buffer!
  []
  (reset! return-buffer nil))

(defonce dispatch-registry! (atom {}))

(defn register-dispatch! [k f]
  (swap! dispatch-registry! assoc k f))

(defn dispatch! [{name :name
                  data :data}]
  (if-let [f (or (get @dispatch-registry! (symbol name))
                 (get @dispatch-registry! (str name)))]
    (apply f data)
    (do (log/infof "No matching function found for: %s " name)

        ;; use this for debug but don't include in production
        
        ;; (println "Available names:")
        ;; (doseq [name (map first (seq @dispatch-registry!))]
        ;;   (println name))
        )))

(defn return-wrapper
  [ptr]
  (let [out-str (java.io.StringWriter.)
        err-str (java.io.StringWriter.)
        out     (atom nil)
        err     (atom nil)
        res     (atom nil)]
    (binding [*out* out-str]
      (try
        (reset! res (->> ptr
                         dt-ffi/c->string
                         ;; change this to something less aggressive if you want to be able to
                         ;; use get/assoc directly
                         (#(json/read-str % :key-fn csk/->kebab-case-keyword))
                         dispatch!))
        (catch Exception e
          (let [message
                (utils/clj->json
                 {:stdout     (str out-str)
                  :stderr     (str err-str)
                  :error      (str e)
                  :stacktrace (try
                                (with-out-str
                                  (.printStackTrace e))
                                (catch Exception e1
                                  nil))})]
            (throw (Exception. message))))
        (finally
          (when-let [out-str (not-empty (str out-str))]
            (reset! out out-str))
          (when-let [err-str (not-empty (str err-str))]
            (swap! err str err-str)))))
    (->> {:stdout @out
          :stderr @err
          :res    @res}
        json/write-str
        dt-ffi/string->c
        (reset! return-buffer))))
