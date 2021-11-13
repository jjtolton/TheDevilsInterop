(ns metal-interop.ffi
  (:gen-class)
  (:require [metal-interop.core :as metal]
            [tech.v3.datatype.ffi :as dt-ffi]
            [clojure.tools.logging :as log]
            clojure.core))

(defn all-modules []
  ;; must be full namespace name, cannot be alias
  '[clojure.core])

(defn all-methods []
  (into {}
        (comp
         (mapcat ns-publics)
         (map (fn [[_ f]]
                [(-> f symbol str) f])))
        (all-modules)))

(defn show-methods! []
  (doseq [method (->> (all-methods) keys (map str))]
    (println method)))

(defn register-methods! []
  (doseq [[symbol f] (seq (all-methods))]
    (metal/register-dispatch! (str symbol) f))
  (metal/register-dispatch! "metal-interop.ffi" all-methods))

(defn initialize! [& args]
  (log/info "Initializing!")
  (register-methods!)
  :ok)

(def response-atom (atom nil))

(defn initialize-json [ptr]
  (initialize!)
  (->> "OK"
       clojure.data.json/write-str
       dt-ffi/string->c
       (reset! response-atom)))

(def response-buffer-ping-test (atom nil))

(defn ping-test! [ptr]
  (let [data (-> ptr dt-ffi/c->string clojure.data.json/read-str)]
    (println data)
    (reset! response-buffer-ping-test
            (-> "OK"
                clojure.data.json/write-str
                dt-ffi/string->c))))

(defn -main []
  (register-methods!))
