(ns gd.examples
  (:require [gd.data :refer :all]
            [clj-time.coerce :as coerce]
            [clj-time.core :as time]
            [incanter.charts :as c]
            [incanter.core :as i]
            [incanter.stats :as s]
            [incanter.svg :as svg]))

(defn ex-0-0 []
  (println "Hello World!"))

(defn ex-1-0 []
  (i/view (unknown-data)))

(defn ex-1-1 []
  (let [data (unknown-data)
       x (i/$ "x" data)
       y (i/$  "y" data) ]
    (-> (c/scatter-plot x y :x-label "x" :y-label "y")
        (i/view))))

(defn sqr [x] (* x x))
(defn y [b m x] (+ (* m x) b))

(defn error-for-line-given-parameters [b m points]
  (let [ ef (fn [p] (- (second p) (y b m (first p))))
         total (reduce + (map sqr (map ef points)))
         c     (count points)]
    (/ total c)))

(defn ex-1-2 []
  (println (error-for-line-given-parameters 0 1 ['(1 1)])))

(defn ex-1-3 []
  (println (error-for-line-given-parameters 1 1 ['(1 1)])))

(defn step-gradient [b-cur m-cur points learning-rate]
  (let [fn-y       (fn [p] (+ (* m-cur (first p)) b-cur))
        fn-e       (fn [p] (- (second p) (fn-y p)))
        fn-db-comp (fn [p] (fn-e p))
        fn-dm-comp (fn [p] (* (first p) (fn-e p)))
        N          (count points)
        db         (* (/ -2 N) (reduce + (map fn-db-comp points)))
        dm         (* (/ -2 N) (reduce + (map fn-dm-comp points)))
        b-new      (- b-cur (* learning-rate db))
        m-new      (- m-cur (* learning-rate dm))]
    (list b-new m-new)))

(defn print-stats [lbl b-cur m-cur points]
  (println lbl " b = " b-cur " and m = " m-cur " and error = " (error-for-line-given-parameters b-cur m-cur points)))

(defn run-gradient-descent [b-start m-start points learning-rate n-iter]
  (let [i n-iter]
    (loop [ndx i
           b-cur b-start
           m-cur m-start
           c 0
           gradient-points (list [b-cur m-cur])
           error-points (list [0 (error-for-line-given-parameters b-cur m-cur points)])]
      (if (> ndx 0)
        (let [ x (step-gradient b-cur m-cur points learning-rate)
               b-new (first x)
               m-new (last x) ]
          (if (= c 0) (print-stats "Starting gradient descent with " b-cur m-cur points))
          (if (= 0 (mod c 1000)) (println "at " c ": " x))
          (recur (- ndx 1) b-new m-new (+ c 1) (conj gradient-points [b-new m-new])
            (conj error-points [(+ c 1) (error-for-line-given-parameters b-new m-new points)])
                ))
        (let []
          (print-stats (str "After " c " iterations")  b-cur  m-cur points)
          (list b-cur m-cur gradient-points error-points))))))

;
; using new few points to draw performance graphs (gradient-points and error-points)
; removing first 15 for error-points as the is dramatic reduction in error initially - so that graph is more understandable
; notice that as the # of iterations, reduction in error is exponential - so ROI is going down
;

(defn ex-1-5 []
  (let [ data (unknown-data)
         points (map (fn [p] (list (:x p) (:y p))) (:rows data))
         x (i/$ "x" data)
         y (i/$ "y" data)
         r (run-gradient-descent -1 0 points 0.0001 100000)
         b-cur (first r)
         m-cur (second r)
         x->y  (fn [x] (+ (* m-cur x) b-cur))
         gradient-points (take-nth 100 (reverse (nth r 2)))
         gx (map first gradient-points)
         gy (map second gradient-points)
         error-points (take-nth 100 (drop 15 (reverse (nth r 3))))
         ex (map first error-points)
         ey (map second error-points)]
    (-> (c/scatter-plot x y :x-label "x" :y-label "y")
      (c/add-function x->y 20 75 :x-label "x" :y-label "y")
      (i/view))
    (-> (c/scatter-plot gx gy :x-label "line y-intercept (B)" :y-label "line slope (M)")
      (i/view))
    (-> (c/scatter-plot ex ey :x-label "Number of iterations" :y-label "error")
      (i/view))))
