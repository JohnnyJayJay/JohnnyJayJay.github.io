(ns cryogen.core
  (:require
   [cryogen-core.compiler :refer [compile-assets-timed]]
   [cryogen-core.plugins :refer [load-plugins]]
   [cryogen.autolink :as al]
   [cryogen.highlight :as hl]
   [cryogen.now :as now]))

(defn compile-site []
  (compile-assets-timed
    {:update-article-fn
     (fn [article config]
       ;; Skip articles with `:ignore? true` in metadata
       (when-not (:ignore? article)
         (hl/highlight-code-in-article (al/autolink-headings article config) config)))
     :extend-params-fn
     (fn [{{{:keys [instance user]} :bookwyrm} :now :as params} _site-data]
       (println "Loading bookshelf for" user "on" instance)
       (assoc-in params [:now :books] (take 3 (now/fetch-bookwyrm-books! instance user))))}))

(defn -main []
  (load-plugins)
  (compile-site)
  (System/exit 0))
