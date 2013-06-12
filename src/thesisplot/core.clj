(ns thesisplot.core
  (:gen-class))
(use '(incanter core charts pdf) '(clojure.java.io) '(clojure-csv core) '(swank core))
(import '(javax.swing JFrame JLabel JTextField JButton)
        '(java.awt.event ActionListener)
        '(java.awt GridLayout)
        '(thesisplot.core CustomRenderer))

(def allKeys [:setCount :objectCount :bucketSize :useClustering :useType :useId :runtime :confidence :grouped])
(def allKeysSet #{:setCount :objectCount :bucketSize :useClustering :useType :useId :runtime :confidence :grouped})
(def allKeysMap {
                  :setCount "sets"
                  :objectCount "objects"
                  :bucketSize "bucket size"
                  :useClustering "use clustering"
                  :useType "use types" 
                  :useId "use ids"
                  :runtime "runtime(ms)"
                  :confidence "confidence"
                  })
(def allKeysUsrMap {
                  "setCount" :setCount
                  "objectCount" :objectCount
                  "bucketSize" :bucketSize
                  "useClustering" :useClustering
                  "useType" :useType
                  "useId" :useId
                  "runtime" :runtime
                  "confidence" :confidence
                  "bar" :bar
                  "line" :line
                  "up" :up
                  "down" :down
                  })
(def default-grouping {:x :objectCount :y :runtime :sort :up :groups [:setCount :useType :useClustering]})

(defn take-csv
  "Takes file name and reads data."
  [fname]
  (with-open [file (clojure.java.io/reader fname)]
    (clojure-csv.core/parse-csv (slurp file))))

(defn parse-item
  [setCount objectCount bucketSize useClustering useType useId runtime confidence]
  {:setCount (Integer/parseInt setCount)
   :objectCount (Integer/parseInt objectCount)
   :bucketSize (Double/parseDouble bucketSize)
   :useClustering (= "1" useClustering)
   :useType (= "1" useType)
   :useId (= "1" useId)
   :runtime (Double/parseDouble runtime)
   :confidence (Double/parseDouble confidence)
   }
  )

(defn maps-to-lists
  [keylist items]
  (map
    #(for [x keylist] (% x))
       items)
  )

(defn data-filter
  [items]
  (filter #(and (true? (:useType %))), items)
  )

(defn make-filter-key
  [items X]
  (dissoc items X)
  )

(defn get-comparator
  [attr]
  (if
    (= attr :confidence)
    <
    >
    )
  )

(defn take-worst-by-attr
  [attr items]
  ( ->> (reduce
          (fn
            [Acc item]
            (let [key (make-filter-key item attr)]
              (if (contains? Acc key)
                (if ((get-comparator attr) (item attr) ((Acc key) attr))
                  (assoc Acc key item)
                  Acc
                  )
                (assoc Acc key item)
                )
              )
            )
          {}
          items
          )
        (map second)
        )
  )

(defmulti apply-group (fn [Acc spec] spec))
(defmethod apply-group :setCount [Acc spec]
  (update-in Acc [:strings] into [(format "%3d" (-> Acc :item :setCount))]))
(defmethod apply-group :objectCount [Acc spec]
  (update-in Acc [:strings] into [(-> Acc :item :objectCount)]))
(defmethod apply-group :bucketSize [Acc spec]
  (update-in Acc [:strings] into [(-> Acc :item :bucketSize)]))
(defmethod apply-group :useType [Acc spec]
  (update-in Acc [:strings] into [(if (true? (-> Acc :item :useType)) "T" "NT")]))
(defmethod apply-group :useClustering [Acc spec]
  (update-in Acc [:strings] into [(if (true? (-> Acc :item :useClustering)) "C" "NC")]))
(defmethod apply-group :default [a b]
  (throw (IllegalArgumentException. (str "Wut?" a b))))

(defn add-group [groupspec item]
  (assoc-in item [:grouped] (apply str (interpose "-" (:strings (reduce apply-group {:item item :strings []} groupspec)))))
  )

(defn add-grouping
  [groupspec items]
  (map #(add-group groupspec %) items)
  )

(defn group-and-filter
  ([items]
   (group-and-filter default-grouping items))
  ([Groupspec items]
   (if (nil? Groupspec) (group-and-filter items)
     (let [usedKeys (clojure.set/union #{(Groupspec :x)} #{(Groupspec :y)} (set (Groupspec :groups)))]
       (let [toRemove (clojure.set/difference allKeysSet usedKeys)]
         [(->> items
               data-filter
              (map #(apply dissoc (into [%] (vec toRemove))))
              (take-worst-by-attr (Groupspec :y))
              (add-grouping (Groupspec :groups))
              )
          (vec (conj usedKeys :grouped))
          ]
         )
       )
     )
   )
  )

(defn group-data
  ([data grouping]
   (let [d (map #(apply parse-item %) data)]
     (let [[grouped-data keyList] (group-and-filter grouping d)]
       (dataset
         keyList
         (->> grouped-data
              (#(if (= (:sort grouping) :up) (sort-by (:x grouping) %)  (reverse (sort-by (:x grouping) %))))
              (sort-by :grouped)
              (maps-to-lists keyList)
              )
         )
       )
     )
   )
  )

(defn get-data
  [filename grouping]
  (group-data (rest (take-csv filename)) grouping)
  )

(def csvd (map #(apply parse-item %) (rest (take-csv "results.csv"))))

(defn set-bar-style
  [chart]
  (let [plot (-> chart .getCategoryPlot) renderer (new CustomRenderer)]
    (do
      (doto plot (.setRenderer renderer))
      chart
    ))
  )

(defn create-bar-chart
  ([filename] (create-bar-chart filename default-grouping))
  ([filename grouping]
   (if (nil? grouping) (create-bar-chart filename)
     (let [data (get-data filename grouping)]
     (set-bar-style (bar-chart
       (sel data :cols (:x grouping))
       (sel data :cols (:y grouping))
       :group-by (sel data :cols :grouped)
       :legend true
       :vertical false
       :x-label (allKeysMap (:x grouping))
       :y-label (allKeysMap (:y grouping))
       ))
     ))
   )
  )

(defn create-line-chart
  ([filename] (create-line-chart filename default-grouping))
  ([filename grouping]
   (if (nil? grouping) (create-line-chart filename)
     (let [data (get-data filename grouping)]
     (line-chart
       (sel data :cols (:x grouping))
       (sel data :cols (:y grouping))
       :group-by (sel data :cols :grouped)
       :legend true
       :x-label (allKeysMap (:x grouping))
       :y-label (allKeysMap (:y grouping))
       )
     ))
   )
  )

(defmulti chart-creator-by-type (fn [t] t))
(defmethod chart-creator-by-type :bar [_] create-bar-chart)
(defmethod chart-creator-by-type :line [_] create-line-chart)
(defmethod chart-creator-by-type :default [_] create-bar-chart)

(defn draw-graph
  [filename & {:keys [grouping]
                :or {grouping default-grouping}}]
    (view (create-bar-chart filename grouping))
  )

(defn parse-keyword
  [kw]
  (allKeysUsrMap kw)
  )

(defn parse-keywords
  [kwds]
  (map parse-keyword (clojure.string/split kwds #"\s"))
  )

(defn draw-graph-with-ui
  ([] (draw-graph-with-ui "results.csv"))
  ([filename & {:keys [grouping]
                :or {grouping default-grouping}}]
  (let [chart (atom ())
        frame (JFrame. "opts")
        input-text (JTextField. "results.csv")
        type-text (JTextField. "bar")
        x-text (JTextField. "bucketSize")
        y-text (JTextField. "runtime")
        groups-text (JTextField. "objectCount useClustering")
        sort-text (JTextField. "up")
        update-button (JButton. "Update")
        width-text (JTextField. "1000")
        height-text (JTextField. "1000")
        file-text (JTextField. "out.png")
        save-button (JButton. "Save")
        ]
    (.addActionListener
      save-button
      (reify ActionListener
        (actionPerformed
          [_ evt]
          (let [width (Integer/parseInt (.getText width-text))
                height (Integer/parseInt (.getText height-text))
                filename (.getText file-text)
                ]
            (save @chart filename :width width :height height)))))
    (.addActionListener
      update-button
      (reify ActionListener
        (actionPerformed
          [_ evt]
          (let [
                f (.getText input-text)
                t (parse-keyword (.getText type-text))
                x (parse-keyword (.getText x-text))
                y (parse-keyword (.getText y-text))
                groups (parse-keywords (.getText groups-text))
                s (parse-keyword (.getText sort-text))
                ]
            (reset! chart ((chart-creator-by-type t) f {:x x :y y :sort s :groups groups}))
            (view @chart)
            ))))
    (doto frame
      (.setLayout (GridLayout. 11 2 3 3))
      (.add input-text)
      (.add (JLabel. "input file"))
      (.add type-text)
      (.add (JLabel. "type"))
      (.add x-text)
      (.add (JLabel. "x"))
      (.add y-text)
      (.add (JLabel. "y"))
      (.add groups-text)
      (.add (JLabel. "groups"))
      (.add sort-text)
      (.add (JLabel. "sort"))
      (.add update-button)
      (.add (JLabel. ""))
      (.add width-text)
      (.add (JLabel. "width"))
      (.add height-text)
      (.add (JLabel. "height"))
      (.add file-text)
      (.add (JLabel. "filename"))
      (.add save-button)
      (.pack)
      (.setVisible true))
    ))
  )

(defn save-graph
  [filename & {:keys [grouping outputfile width height]
                :or {grouping default-grouping outputfile "out.png" width 1000 height 1000}}]
    (save (create-bar-chart filename grouping) outputfile :width width :height height)
  )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (draw-graph-with-ui "results.csv"))
