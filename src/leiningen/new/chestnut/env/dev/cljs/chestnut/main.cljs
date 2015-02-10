(ns {{project-ns}}.main
  (:require [{{project-ns}}.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [cljs.core.async :refer [put!]]
            [weasel.repl :as weasel]))

{{#isomorphic?}}
(if (exists? js/console)
{{/isomorphic?}}
 (enable-console-print!)
{{#isomorphic?}}
 (set-print-fn! js/print))

(defn ^:export start []
{{/isomorphic?}}

(def hostname (-> js/window .-location .-hostname))

(figwheel/watch-and-reload
  :websocket-url (str "ws://" hostname ":3449/figwheel-ws")
  :jsload-callback (fn [] (core/main)))

(weasel/connect (str "ws://" hostname ":9001") :verbose true :print #{:repl :console})

(core/main)

{{#isomorphic?}}
)
{{/isomorphic?}}
