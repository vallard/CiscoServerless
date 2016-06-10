(ns mashape1.core
    (:require-macros [reagent.ratom :refer [reaction]]
                    [adzerk.env :as env])
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [ajax.core :refer [GET POST]]
              [re-frame.core :refer [dispatch dispatch-sync register-sub path subscribe register-handler ]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

;; env
(env/def
  MASHAPE_KEY :required
)


;; --- handlers
(register-handler
  :initialize
  (fn [_ _] {
    :skiinfo {:status "fetching data..."}
  }))

; make call to the mashape api
(register-handler
  :getskispots
  (fn
    [db _]
    (GET 
     "https://makevoid-skicams.p.mashape.com/cams.json"
     {:headers [:X-Mashape-Key MASHAPE_KEY :Accept "application/json"] 
      :response-format :json
      :keywords? true
      :handler #(dispatch [:processski %1])})
    db))
    
(register-handler
  :processski
  (fn
    [db [_ response]]
    (-> db
      (assoc :skiinfo (js->clj response))
)))
    

;(register-handler
;  :skiinfo
;  (fn 
;    [db [_ value]]
;    (assoc db :skiinfo value)))  


(register-sub 
  :skiinfo
  (fn [db _]
    (reaction (:skiinfo @db))))


;; -------------------------
;; Views
(defn show-pictures 
  [cams]
  (do 
    (let [k (keys cams)]
      (for [a k]
        (let [camera (get cams a)]
          [:div  { :class "well" }
            [:h3 (get camera :name)]
            [:img {:src (get camera :url) :width "200px" }]
          ]
        )
      )
    )
  )
)

(defn show-spots [skimap]
  (do 
    (let [k (keys skimap)]
      (for [a k]
        (let [spot (get skimap a)]
          [:div {:class "well" }
            [:h1 (get spot :name)]  
            [:div (show-pictures (get spot :cams))]
            ;[:small (str spot) ]
          ] 
        )
      )
  ))
)



(defn home-page []
  [:div {:class "container" }
    [:h1 "Ski Italy!"]
    (let [ski (subscribe [:skiinfo])]
      (show-spots @ski))
    
])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))


;; -------------------------
;; Initialize app

(defn mount-root []
  (dispatch-sync [:initialize])
  (dispatch-sync [:getskispots])
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
