(ns locus.order.lattice.core.morphism
  (:require [locus.base.logic.core.set :refer :all]
            [locus.base.logic.limit.product :refer :all]
            [locus.base.function.core.object :refer :all]
            [locus.base.logic.structure.protocols :refer :all]
            [locus.elementary.copresheaf.core.protocols :refer :all]
            [locus.quiver.relation.binary.br :refer :all]
            [locus.quiver.relation.binary.product :refer :all]
            [locus.quiver.binary.core.object :refer :all]
            [locus.elementary.quiver.unital.object :refer :all]
            [locus.order.general.core.object :refer :all]
            [locus.order.general.core.morphism :refer :all]
            [locus.order.lattice.core.object :refer :all]
            [locus.quiver.base.core.protocols :refer :all])
  (:import (locus.order.lattice.core.object Lattice)))

; The category of lattices is distinguished from the category of categories,
; by its very special type of functors which are the lattice morphisms. These
; morphisms of lattices also need to preserve products and coproducts.
(deftype LatticeMorphism
  [source target func]

  AbstractMorphism
  (source-object [this] source)
  (target-object [this] target)

  StructuredDifunction
  (first-function [this]
    (->SetFunction
      (objects source)
      (objects target)
      func))
  (second-function [this]
    (->SetFunction
      (morphisms source)
      (morphisms target)
      (fn [pair]
        (map func pair))))

  ; Functional aspects of lattice homomorphisms
  ConcreteMorphism
  (inputs [this] (underlying-set source))
  (outputs [this] (underlying-set target))

  clojure.lang.IFn
  (invoke [this arg]
    (func arg))
  (applyTo [this args]
    (clojure.lang.AFn/applyToHelper this args)))

; The hierarchy of lattice morphisms
(derive LatticeMorphism :locus.elementary.copresheaf.core.protocols/monotone-map)

; Composition and identities in the category of lattices
(defmethod compose* LatticeMorphism
  [a b]

  (LatticeMorphism.
    (source-object b)
    (target-object a)
    (comp (.func a) (.func b))))

(defmethod identity-morphism Lattice
  [lattice]

  (LatticeMorphism. lattice lattice identity))

; Convert a lattice homomorphism into a monotone map
(defmethod to-monotone-map LatticeMorphism
  [^LatticeMorphism lattice-homomorphism]

  (->MonotoneMap
    (source-object lattice-homomorphism)
    (target-object lattice-homomorphism)
    (.-func lattice-homomorphism)))

