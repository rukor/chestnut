(ns {{project-ns}}.core
  (:require [om.core :as om :include-macros true]{{{core-cljs-requires}}}{{#isomorphic?}}
            [{{project-ns}}.routes :refer [app-routes]]
            [com.firstlinq.om-ssr.state :refer [get-state]]
            [com.firstlinq.om-ssr.state.transit :refer [deserialise]]
            [com.firstlinq.om-ssr.router :refer [path-for path-exists? navigate-to link]]
            [com.firstlinq.om-ssr.router.silk :refer [silk-router]]{{/isomorphic?}}))

(defonce app-state (atom {:text "Hello Chestnut!"}))

{{#not-isomorphic?}}
 (defn app-view [app owner]
   (reify
     om/IRender
     (render [_]
             (dom/h1 {{#not-om-tools?}}nil {{/not-om-tools?}}(:text app)))))
{{/not-isomorphic?}}
{{#isomorphic?}}

(defonce router (silk-router app-routes))

(defmethod get-state :default [route-id route-params]
   (swap! app-state assoc :route {:id route-id :params route-params}))

(defn menu [data owner {:keys [router]}]
  (reify om/IRender
     (render [_]
       (dom/ul nil
               (dom/li nil (link router {:route [:home {}]} (dom/span "Home")))
               (dom/li nil (link router {:href (path-for router :page {:page-id "one"})}
                                 (dom/span nil "Page One")))
               (dom/li nil (link router {:route [:page {:page-id "two"}]}
                                 (dom/span nil "Page Two")))))))

(defn home [data owner]
  (reify om/IRender
     (render [_]
       (dom/div "Home Page"))))

(defn page [data owner]
  (reify om/IRender
     (render [_]
       (dom/div "Page ID: " (get-in data [:route :params :page-id])))))

(def pages {:home home :page page})

(defn app-view [app owner]
   (reify
     om/IRender
     (render [_]
             (dom/div
               (om/build menu {} {:opts {:router router}})
               (when-let [view (get pages (get-in app [:route :id]))]
                         (om/build view app {}))))))

(defn ^:export render-to-string
     "Takes an app state as transit+json and returns the HTML for that state."
     [state-string]
     (->> (deserialise state-string)
          (om/build app-view)
          (om.dom/render-to-str)))
{{/isomorphic?}}

(defn main []
{{#isomorphic?}}
  (->> (.getElementById js/document "app-state")
       (.-textContent)
       (deserialise)
       (reset! app-state))
{{/isomorphic?}}
  (om/root
    app-view
    app-state
    {:target (.getElementById js/document "app")}))
