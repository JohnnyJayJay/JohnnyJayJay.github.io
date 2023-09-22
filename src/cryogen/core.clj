(ns cryogen.core
  (:require
   [clojure.edn :as edn]
   [cryogen-core.compiler :refer [compile-assets-timed]]
   [cryogen-core.plugins :refer [load-plugins]]
   [cryogen.autolink :as al]
   [cryogen.highlight :as hl]
   [cryogen.openring :as or]))

(defn compile-site []
  (compile-assets-timed
    {:update-article-fn
     (fn update-article [article config]
       ;; Skip articles with `:ignore? true` in metadata
       (when-not (:ignore? article)
         (hl/highlight-code-in-article (al/autolink-headings article config) config)))}))

(defn -main []
  (load-plugins)
  (compile-site)
  (System/exit 0))
