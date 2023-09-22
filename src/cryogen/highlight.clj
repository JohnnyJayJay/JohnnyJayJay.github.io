;; SPDX-License-Identifier: MIT
;; SPDX-FileCopyrightText: 2023 JohnnyJayJay
;;
;; AOT syntax highlighting for cryogen blogs using highlight.js and GraalVM polyglot features.
;; How to use:
;; 1. Add Graaljs as a dependency: https://mvnrepository.com/artifact/org.graalvm.js/js/23.0.1 (or build your blog on GraalVM directly)
;; 2. Place a minimal highlight.js distribution at the root of your cryogen project. You can get one by downloading without selecting any languages from https://highlightjs.org/download
;; 3. In your cryogen.core namespace, call `compile-assets-timed` with `{:update-article-fn highlight-code-in-article}`
;; 4. Remove the highlight.js scripts from your theme's templates - they're not needed anymore. A highlight.js *theme* is still needed though!
(ns cryogen.highlight
  (:require [net.cgrand.enlive-html :as enlive]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:import (org.graalvm.polyglot Context Source)))

;; "Wrong" language names that you use in your code blocks anyway, and their mapping to the "correct" language.
(def aliases
  "Mapping from language alias -> actual language name, for cases where you specify
  'wrong' languages in your code blocks that wouldn't normally be recognized by highlight.js."
  {"clj" "clojure"
   "js" "javascript"})

(def ^Context js-context
  (doto (.build (Context/newBuilder (into-array ["js"])))
    (.eval (.build (Source/newBuilder "js" (io/file "highlight.js"))))))

;; Atom keeping track of what languages have already been fetched.
(def loaded-languages
  (atom #{}))

;; Get the hljs grammar for a (recognized) language from a CDN. You can of course use a different CDN or rewrite this to load files from a local directory instead.
(defn fetch-grammar! [language]
  (try
    (slurp (format "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/languages/%s.min.js" language))
    (catch java.io.FileNotFoundException _e
      (printf "WARNING: A code block specifies '%s' as its language, which is not recognized. Using automatic highlighting instead.\n" language)
      nil)))

(defn ensure-language! [language]
  (let [language (get aliases language language)]
    (if (contains? @loaded-languages language)
      language
      (when-let [grammar (fetch-grammar! language)]
        (.eval js-context "js" grammar)
        (swap! loaded-languages conj language)
        language))))

(defn highlight-code
  "Highlights code using the given language, downloading and loading the language grammar if necessary.
  Both `code` and the returned value are strings. Pass `nil` as the language for automatic highlighting."
  [language code]
  ;; Locking because JS context is not thread safe.
  (locking js-context
    (let [language (ensure-language! language)]
      (.. js-context (getBindings "js") (putMember "hlCode" code))
      (.asString
       (.eval
        js-context
        "js"
        (if language
          (format "hljs.highlight(hlCode, { language: '%s', ignoreIllegals: true }).value" language)
          "hljs.highlightAuto(hlCode).value"))))))

(defn apply-highlighting-to-dom [dom]
  (enlive/transform
   dom
   [:code]
   (fn [selected-node]
     (let [language-class (get-in selected-node [:attrs :class])]
       (if (and language-class (not (str/includes? language-class "nohighlight")))
         (-> selected-node
             (update :content (comp enlive/html-snippet (partial highlight-code language-class) first))
             (update-in [:attrs :class] str " hljs"))
         selected-node)))))

(defn highlight-code-in-article
  "Function that can be passed as :update-article-fn and that updates the content-dom of an article to apply highlighting."
  [article _]
  (update article :content-dom apply-highlighting-to-dom))
