(ns stateful.core-test
  (:require [clojure.test.check
             [generators :as gen]
             [properties :as prop]
             [clojure-test :refer [defspec]]]
            [clojure.test.check :as tc]
            [clojure.test :refer :all]
            [stateful.core :as stateful]))

;; ## Basic Behaviour

(def ascending-integers
  (stateful/generator
    (gen/vector
      (gen/let [delta    gen/s-pos-int
                previous (stateful/value [:previous])]
        (let [value (+ delta previous)]
          (stateful/return value {:previous value}))))
    {:previous 0}))

(defspec t-stateful-generator-behaviour 200
  (prop/for-all
    [asc-ints ascending-integers]
    (and (every? integer? asc-ints)
         (distinct? asc-ints)
         (or (not (next asc-ints))
             (apply <= asc-ints)))))

(deftest t-stateful-generator-shrinking
  (testing "simple shrinking case."
    (let [prop (prop/for-all
                 [asc-ints ascending-integers]
                 (not-any? #{100} asc-ints))
          result (is (tc/quick-check 500 prop))]
      (is (false? (:result result)))
      (is (= [[100]] (-> result :shrunk :smallest)))))
  (testing "multi-element shrinking case."
    (let [prop (prop/for-all
                 [asc-ints ascending-integers]
                 (<= (count asc-ints) 5))
          result (is (tc/quick-check 500 prop))
          shrunk (-> result :shrunk :smallest first)]
      (is (false? (:result result)))
      (is (= (sort shrunk) shrunk))
      (is (= (distinct shrunk) shrunk))))
  (testing "shrinking case for combination of stateful generators."
    (let [prop (prop/for-all
                 [ints (->> (gen/vector ascending-integers)
                            (gen/fmap #(reduce into %)))]
                 (let [freq (frequencies ints)]
                   (<= (freq 100 0) 2)))
          result (is (tc/quick-check 200 prop))]
      (is (false? (:result result)))
      (is (= [[100 100 100]] (-> result :shrunk :smallest))))))

(defspec t-stateful-generator-return 200
  (prop/for-all
    [[asc-ints final-state] (stateful/generator
                              (gen/tuple (gen/not-empty ascending-integers)
                                         (stateful/state)))]
    (= (:previous final-state) (last asc-ints))))

;; ## Common Uses

(defspec t-unique-generator 200
  (prop/for-all
    [values (stateful/generator
              (gen/vector
                (stateful/unique ::seen gen/int)))]
    (= (count values) (count (distinct values)))))

(deftest t-unique-generator-shrinking
  (let [prop (prop/for-all
               [values (stateful/generator
                         (gen/vector
                           (stateful/unique ::seen gen/int)))]
               (<= (count values) 5))
        result (is (tc/quick-check 500 prop))
        shrunk (-> result :shrunk :smallest first)]
    (is (false? (:result result)))
    (is (= (sort shrunk) (sort (distinct shrunk))))))
