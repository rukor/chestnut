(ns {{project-ns}}.main
  (:require [{{project-ns}}.core :as core]))


{{#isomorphic?}}
(defn ^:export start []
{{/isomorphic?}}

(core/main)

{{#isomorphic?}}
)
{{/isomorphic?}}