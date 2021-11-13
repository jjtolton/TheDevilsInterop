(ns metal-interop.main
    (:gen-class)
    (:require metal-interop.build
              metal-interop.ffi))

(defn -main []
  (metal-interop.build/build!))

