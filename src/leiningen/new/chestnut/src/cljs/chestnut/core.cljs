(ns {{project-ns}}.core
  (:require [om.core :as om :include-macros true]{{{core-cljs-requires}}}{{#isomorphic?}}
            [fl.lib.client.router :refer [path-for]]
            [fl.lib.client.router.silk :refer [silk-router]]
            [fl.lib.client.util :refer [deserialise]]
            [fl.lib.client.util.dom :refer [by-id]]
            [fl.lib.client.util.om :refer [link]]
            [fl.lib.client.router.navigation :refer [make-navigator]]{{/isomorphic?}}))

(defonce app-state (atom {:text "Hello Chestnut!"}))

{{#not-isomorphic?}}
 (defn app-view [app owner]
   (reify
     om/IRender
     (render [_]
             (dom/h1 {{#not-om-tools?}}nil {{/not-om-tools?}}(:text app)))))
{{/not-isomorphic?}}
{{#isomorphic?}}
 (def app-routes
   {:home [[]]
    :page [["page" :page-id]]})

 (defonce router
   (silk-router app-routes (fn [route-id route-map]
                  (swap! app-state assoc :route-id route-id :params route-map))))

(defn menu [data owner {:keys [navigator router]}]
  (reify om/IRender
     (render [_]
       (let [link (partial link navigator)]
          (dom/ul nil
            (dom/li nil (link {:href (path-for router :home {})} (dom/span "Home")))
            (dom/li nil (link {:href (path-for router :page {:page-id "one"})}
                     (dom/span nil "Page One"))))))))

(defn home [data owner]
  (reify om/IRender
     (render [_]
       (dom/div "Home Page"))))

(defn page [data owner]
  (reify om/IRender
     (render [_]
       (dom/div "Page ID: " (get-in data [:params :page-id])))))

(def pages {:home home :page page})

(defn app-view [app owner]
   (reify
     om/IRender
     (render [_]
             (dom/div
               (om/build menu {} {:opts {:navigator (make-navigator router)
                                         :router    router}})
               (when-let [view (get pages (:route-id app))]
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
  (->> (by-id "app-state") (.-textContent) (deserialise) (reset! app-state))
{{/isomorphic?}}
  (om/root
    app-view
    app-state
    {:target (by-id "app")}))
