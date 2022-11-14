(ns locus.grothendieck.topology.core.morphism
  (:require [locus.base.logic.core.set :refer :all]
            [locus.base.function.core.object :refer :all]
            [locus.base.logic.structure.protocols :refer :all]
            [locus.elementary.copresheaf.core.protocols :refer :all]
            [locus.grothendieck.topology.core.object :refer :all]
            [locus.elementary.preorder.core.object :refer :all]
            [locus.elementary.preorder.core.morphism :refer :all])
  (:import (locus.grothendieck.topology.core.object TopologicalSpace)))

; We say that a map of topological spaces f: X -> Y is a continuous map
; provided that f reflects all open sets in Y. Then the composition of
; continuous maps is again continuous, so they form morphisms in the category
; Top of topological spaces and continuous maps.
(deftype ContinuousMap [in out func]
  AbstractMorphism
  (source-object [this] in)
  (target-object [this] out)

  ConcreteMorphism
  (inputs [this] (underlying-set in))
  (outputs [this] (underlying-set out))

  clojure.lang.IFn
  (invoke [this arg] (func arg))
  (applyTo [this args] (clojure.lang.AFn/applyToHelper this args)))

; The adjoint relationship between order and topology
(defn specialization-monotone-map
  [continuous-map]

  (->MonotoneMap
    (specialization-preorder (source-object continuous-map))
    (specialization-preorder (target-object continuous-map))
    (fn [i]
      (continuous-map i))))

(defn alexandrov-continuous-function
  [monotone-map]

  (->ContinuousMap
    (alexandrov-topology (source-object monotone-map))
    (alexandrov-topology (target-object monotone-map))
    (fn [i]
      (monotone-map i))))

; Identities and composition in the category Top
(defmethod identity-morphism TopologicalSpace
  [topology]

  (ContinuousMap. topology topology identity))

(defmethod compose* ContinuousMap
  [a b]

  (ContinuousMap.
    (source-object b)
    (target-object a)
    (comp (.func a) (.func b))))

; Topological images and inverse images form an adjoint relationship in topology
(defn topological-image
  [function source-topology]

  (let [output-opens (set
                       (filter
                         (fn [out-set]
                           (contains? (.-opens source-topology) (set-inverse-image function out-set)))
                         (power-set (outputs function))))]
    (->TopologicalSpace
      (outputs function)
      output-opens)))

(defn topological-inverse-image
   [function target-topology]

  (let [input-opens (set
                      (map
                        (fn [out-set]
                          (set-inverse-image function out-set))
                        (.-opens target-topology)))]
    (->TopologicalSpace
      (inputs function)
      input-opens)))

; Ontology of continuous maps
(defn continuous-map?
  [morphism]

  (= (type morphism) ContinuousMap))