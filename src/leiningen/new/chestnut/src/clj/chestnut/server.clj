(ns {{project-ns}}.server
  (:require [clojure.java.io :as io]
            [{{project-ns}}.dev :refer [is-dev? inject-devmode-html browser-repl start-figwheel{{less-sass-refer}}]] {{#isomorphic?}}
            [{{project-ns}}.routes :refer [app-routes]]
            [com.firstlinq.om-ssr.render :refer [create-render-fn]]
            [com.firstlinq.om-ssr.ring :refer [create-ring-handler]]
            [com.firstlinq.om-ssr.state.transit :refer [serialise deserialise]]
            ;; TODO make silk optional (bidi could be used as well)
            [com.firstlinq.om-ssr.state.silk :refer [create-request->state]]{{/isomorphic?}}
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources]]
            [net.cgrand.enlive-html :refer [deftemplate content html-content]]
            [net.cgrand.reload :refer [auto-reload]]
            [ring.middleware.reload :as reload]
            [ring.middleware.defaults :refer [wrap-defaults {{ring-defaults}}]]
            [environ.core :refer [env]]{{{server-clj-requires}}}))

(deftemplate page (io/resource "index.html") [{{#isomorphic?}}app-state html{{/isomorphic?}}]
  [:body]
  (if is-dev? inject-devmode-html identity)
  {{#isomorphic?}}

  [:script#app-state]
  (content app-state)

  [:div#app]
  (html-content html){{/isomorphic?}})

(defn make-routes [handler]
  (compojure.core/routes
    (resources "/")
    (resources "/react" {:root "react"})
    (GET "/*" [] handler)))

(defn http-handler []
  (let [req->state (create-request->state app-routes)
        render-fn (create-render-fn "{{project-ns}}.core" "render_to_string" :is-dev? is-dev?)
        handler-fn (create-ring-handler req->state render-fn serialise page)
        routes (make-routes handler-fn)]
   (if is-dev?
      (reload/wrap-reload (wrap-defaults routes {{ring-defaults}}))
      (wrap-defaults routes {{ring-defaults}}))))

(defn run-web-server [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (print "Starting web server on port" port ".\n")
    ({{server-command}} (http-handler) {:port port :join? false})))

(defn run-auto-reload [& [port]]
  (auto-reload *ns*)
  (start-figwheel){{less-sass-start}})

(defn run [& [port]]
  (when is-dev?
    (run-auto-reload))
  (run-web-server port))

(defn -main [& [port]]
  (run port))
