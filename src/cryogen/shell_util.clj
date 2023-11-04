(ns cryogen.shell-util
  (:require [clojure.java.shell :as shell]))

(defn sh [& args]
  (let [{:keys [exit err out]} (apply shell/sh args)]
    (binding [*out* *err*]
      (println err))
    (if (zero? exit)
      out
      (System/exit exit))))
