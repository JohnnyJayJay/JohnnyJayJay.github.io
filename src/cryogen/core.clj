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
       (-> params
           (assoc-in [:now :books] (now/fetch-bookwyrm-books! instance user))
           ;; TODO comment back in once the spotify worflow works
           #_(assoc-in [:now :spotify] (now/fetch-spotify-top! (System/getenv "SPOTIFY_ACCESS_TOKEN")))))}))

(defn -main []
  (load-plugins)
  (compile-site)
  (System/exit 0))
