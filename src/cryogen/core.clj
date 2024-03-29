(ns cryogen.core
  (:require
   [clojure.pprint :refer [pprint]]
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
       (println "Fetching bookshelf and spotify data")
       (try
         (-> params
           (assoc-in [:now :books] (now/fetch-bookwyrm-books! instance user))
           (assoc-in [:now :spotify] (now/fetch-spotify-top! (System/getenv "SPOTIFY_ACCESS_TOKEN"))))
         (catch Exception e
           (let [{:keys [uri status body]} (ex-data e)]
             (println "Error preparing now page: status" status "for URI" uri)
             (pprint body)
             params))))}))

(defn -main []
  (load-plugins)
  (compile-site)
  (System/exit 0))
