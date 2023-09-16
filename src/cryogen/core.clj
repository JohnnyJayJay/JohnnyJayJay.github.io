(ns cryogen.core
  (:require [cryogen-core.compiler :refer [compile-assets-timed]]
            [cryogen-core.plugins :refer [load-plugins]]
            [net.cgrand.enlive-html :as enlive])
  (:import (java.io StringReader)))

;;------------------------------------------------------------ autolink-headings

(defn permalink-node [{{heading-id :id} :attrs :as heading} blog-prefix]
  (first
    (enlive/html
      [:a {:href (str "#" heading-id)
           :aria-label (str "Permalink to " (enlive/text heading))
           :role "link"
           :class "anchor"}
       [:img {:src (str blog-prefix "/icons/link.svg") :class "adjust-for-dark" :aria-hidden true :focusable false :width 20}]])))

(defn autolink-content-headings [content blog-prefix]
  (let [content-nodes (enlive/html-resource (StringReader. content))]
    (-> content-nodes
        (enlive/transform
          [#{:h1 :h2 :h3 :h4 :h5 :h6}]
          (fn autolink-heading [heading]
            (update heading
                    :content
                    #(apply vector (permalink-node heading blog-prefix) %))))
        (enlive/emit*)
        (->> (apply str)))))

(defn autolink-headings [article {:keys [blog-prefix]}]
  (update article :content autolink-content-headings blog-prefix))

;;---------------------------------------------------------------------

(defn compile-site []
  (compile-assets-timed
    {:postprocess-article-html-fn
     (fn update-article [article config]
       ;; Skip articles with `:ignore? true` in metadata
       (when-not (:ignore? article)
         (autolink-headings article config)))}))

(defn -main []
  (load-plugins)
  (compile-site)
  (System/exit 0))
