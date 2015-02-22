(ns {{project-ns}}.dev
  (:require [environ.core :refer [env]]
            [net.cgrand.enlive-html :refer [set-attr prepend append html]]
            [cemerick.piggieback :as piggieback]
            [weasel.repl.websocket :as weasel]
            [figwheel-sidecar.auto-builder :as fig-auto]
            [figwheel-sidecar.core :as fig]
            [clojurescript-build.auto :as auto]
            [clojure.java.shell :refer [sh]]))

(def is-dev? (env :is-dev))

(def inject-devmode-html
  (set-attr :class "is-dev"))

(defn browser-repl []
  (let [repl-env (weasel/repl-env :ip "0.0.0.0" :port 9001)
        repl-opts {}]
    (piggieback/cljs-repl :repl-env repl-env)
    (piggieback/cljs-eval repl-opts repl-env '(in-ns '{{project-ns}}.core) {})))

(defn start-figwheel []
  (let [server (fig/start-server { :css-dirs ["resources/public/css"] })
        config {:builds [{:id "dev"
                          :source-paths  [{{{cljx-cljsbuild-spath}}} "src/cljs" "env/dev/cljs"]
                          :compiler {:main                 '{{project-ns}}.main
                                     :output-to            "resources/public/js/app.js"
                                     :output-dir           "resources/public/js/out"
                                     :asset-path           "/js/out"
                                     :optimizations        :none
                                     :source-map           "resources/public/js/out.js.map"
                                     :source-map-timestamp true
                                     }}]
                :figwheel-server server}]
    (fig-auto/autobuild* config)))
{{#less?}}
(defn start-less []
  (future
    (println "Starting less.")
    (sh "lein" "less" "auto")))
{{/less?}}
{{#sass?}}
(defn start-sass []
  (future
    (println "Starting sass.")
    (sh "lein" "auto" "sassc" "once")))
{{/sass?}}