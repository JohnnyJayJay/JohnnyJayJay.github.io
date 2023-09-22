(ns cryogen.autolink
  (:require [net.cgrand.enlive-html :as enlive]))

(defn permalink-node [{{heading-id :id} :attrs :as heading} blog-prefix]
  (first
    (enlive/html
      [:a {:href (str "#" heading-id)
           :aria-label (str "Permalink to " (enlive/text heading))
           :role "link"
           :class "anchor"}
       [:img {:src (str blog-prefix "/icons/link.svg") :class "adjust-for-dark" :aria-hidden true :focusable false :width 20}]])))

(defn autolink-content-headings [content blog-prefix]
  (-> content
      (enlive/transform
       [#{:h1 :h2 :h3 :h4 :h5 :h6}]
       (fn autolink-heading [heading]
         (update
          heading
          :content
          #(apply vector (permalink-node heading blog-prefix) %))))))

(defn autolink-headings [article {:keys [blog-prefix]}]
  (update article :content-dom autolink-content-headings blog-prefix))
