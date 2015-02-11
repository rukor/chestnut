(ns {{project-ns}}.server
  (:require [clojure.java.io :as io]
            [{{project-ns}}.dev :refer [is-dev? inject-devmode-html browser-repl start-figwheel{{less-sass-refer}}]] {{#isomorphic?}}
            [fl.lib.server.ssr.render :refer [render-fn]]
            [fl.lib.server.ssr.state :refer [route-state-handler]]
            [domkm.silk :as silk]
            [domkm.silk.serve :refer [ring-handler]]{{/isomorphic?}}
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources]]
            [net.cgrand.enlive-html :refer [deftemplate content html-content]]
            [net.cgrand.reload :refer [auto-reload]]
            [ring.middleware.reload :as reload]
            [ring.middleware.defaults :refer [wrap-defaults {{ring-defaults}}]]
            [environ.core :refer [env]]{{{server-clj-requires}}}))

(deftemplate page (io/resource "index.html") [{{#isomorphic?}}state-string rendered{{/isomorphic?}}]
  [:body]
  (if is-dev? inject-devmode-html identity)
  {{#isomorphic?}}

  [:script#app-state]
  (content state-string)

  [:div#app]
  (html-content rendered){{/isomorphic?}})

{{#not-isomorphic?}}
(def handler page)
{{/not-isomorphic?}}
{{#isomorphic?}}
 (def app-routes
   {:home [[]]
    :page [["page" :page-id]]})

(defn params-fn
 [request]
 (apply dissoc (:params request)
        ::silk/url ::silk/routes (keys request)))

(defn route-handler [f]
 (route-state-handler page f :params-fn params-fn))

(def handler
 (->> (render-fn "{{name}}.core" "render_to_string" :initial-pool-size 3 :is-dev? is-dev?)
      (route-handler)
      (ring-handler app-routes)))
{{/isomorphic?}}

(defroutes routes
  (resources "/")
  (resources "/react" {:root "react"})
  (GET "/*" [] handler))

(def http-handler
  (if is-dev?
    (reload/wrap-reload (wrap-defaults #'routes {{ring-defaults}}))
    (wrap-defaults routes {{ring-defaults}})))

(defn run-web-server [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (print "Starting web server on port" port ".\n")
    ({{server-command}} http-handler {:port port :join? false})))

(defn run-auto-reload [& [port]]
  (auto-reload *ns*)
  (start-figwheel){{less-sass-start}})

(defn run [& [port]]
  (when is-dev?
    (run-auto-reload))
  (run-web-server port))

(defn -main [& [port]]
  (run port))
