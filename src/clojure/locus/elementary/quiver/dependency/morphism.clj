(ns locus.elementary.quiver.dependency.morphism
  (:require [locus.base.logic.core.set :refer :all]
            [locus.base.logic.limit.product :refer :all]
            [locus.base.sequence.core.object :refer :all]
            [locus.base.partition.core.setpart :refer :all]
            [locus.base.function.core.object :refer :all]
            [locus.base.logic.structure.protocols :refer :all]
            [locus.elementary.copresheaf.core.protocols :refer :all]
            [locus.quiver.relation.binary.product :refer :all]
            [locus.quiver.relation.binary.br :refer :all]
            [locus.quiver.relation.binary.mbr :refer :all]
            [locus.quiver.relation.binary.sr :refer :all]
            [locus.quiver.binary.core.object :refer :all]
            [locus.quiver.binary.core.morphism :refer :all]
            [locus.elementary.quiver.permutable.object :refer :all]
            [locus.elementary.quiver.permutable.morphism :refer :all]
            [locus.elementary.quiver.unital.object :refer :all]
            [locus.elementary.quiver.unital.morphism :refer :all]
            [locus.elementary.quiver.dependency.object :refer :all]
            [locus.quiver.base.core.protocols :refer :all])
  (:import [locus.base.function.core.object SetFunction]
           (locus.elementary.quiver.dependency.object DependencyQuiver)))

; Dependency quivers are simply presheaves over the category consisting of two objects and
; eight morphisms: the source, target, reverse, source identity, target identity, identity
; vertex identity, and edge identity morphisms with their obvious compositions. Then morphisms in
; of these quivers are the corresponding morphisms of presheaves.

; Morphisms in the topos of dependency quivers
(deftype MorphismOfDependencyQuivers [source-quiver target-quiver input-function output-function]
  AbstractMorphism
  (source-object [this] source-quiver)
  (target-object [this] target-quiver)

  StructuredDifunction
  (first-function [this] input-function)
  (second-function [this] output-function))

(derive MorphismOfDependencyQuivers :locus.elementary.copresheaf.core.protocols/morphism-of-structured-dependency-quivers)

; A structured morphism of dependency quivers is a morphism in a category equipped with a functor
; to the topos of dependency quivers. An example is a morphism in the category of groupoids.
; The underlying-morphism-of-dependency-quivers method gets you this
(defmulti underlying-morphism-of-dependency-quivers type)

(defmethod underlying-morphism-of-dependency-quivers MorphismOfDependencyQuivers
  [^MorphismOfDependencyQuivers quiver] quiver)

(defmethod underlying-morphism-of-dependency-quivers :default
  [morphism]

  (->MorphismOfDependencyQuivers
    (underlying-dependency-quiver (source-object morphism))
    (underlying-dependency-quiver (target-object morphism))
    (first-function morphism)
    (second-function morphism)))

; Components of morphisms of permutable quivers
(defmethod get-set MorphismOfDependencyQuivers
  [morphism [i v]]

  (case i
    0 (get-set (source-object morphism) v)
    1 (get-set (target-object morphism) v)))

(defmethod get-function MorphismOfDependencyQuivers
  [morphism [[i j] v]]

  (let [source-data* [0 1 0 0 1 0 0 0]]
    (case [i j]
      [0 0] (get-function (source-object morphism) v)
      [1 1] (get-function (target-object morphism) v)
      [0 1] (compose
              (get-function (target-object morphism) v)
              (morphism-of-quivers-component-function morphism (get source-data* v))))))

; Composition and morphisms in the topos of dependency quivers
(defmethod compose* MorphismOfDependencyQuivers
  [a b]

  (MorphismOfDependencyQuivers.
    (source-object b)
    (target-object a)
    (compose-functions (first-function a) (first-function b))
    (compose-functions (second-function a) (second-function b))))

(defmethod identity-morphism DependencyQuiver
  [quiv]

  (MorphismOfDependencyQuivers.
    quiv
    quiv
    (identity-function (first-set quiv))
    (identity-function (second-set quiv))))

; Products and coproducts in the topos of morphisms of quivers
(defmethod product MorphismOfDependencyQuivers
  [& args]

  (->MorphismOfDependencyQuivers
    (apply product (map source-object args))
    (apply product (map target-object args))
    (apply product (map first-function args))
    (apply product (map second-function args))))

(defmethod coproduct MorphismOfDependencyQuivers
  [& args]

  (->MorphismOfDependencyQuivers
    (apply coproduct (map source-object args))
    (apply coproduct (map target-object args))
    (apply coproduct (map first-function args))
    (apply coproduct (map second-function args))))

; Morphisms in the topos of dependency quivers
(defn morphism-of-dependency-quivers?
  [morphism]

  (= (type morphism) MorphismOfDependencyQuivers))