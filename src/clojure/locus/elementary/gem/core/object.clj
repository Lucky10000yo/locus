(ns locus.elementary.gem.core.object
  (:require [locus.elementary.logic.base.core :refer :all]
            [locus.elementary.logic.order.seq :refer :all]
            [locus.elementary.relation.binary.product :refer :all]
            [locus.elementary.relation.binary.br :refer :all]
            [locus.elementary.relation.binary.sr :refer :all]
            [locus.elementary.incidence.system.setpart :refer :all]
            [locus.elementary.function.core.object :refer :all]
            [locus.elementary.function.core.protocols :refer :all]
            [locus.elementary.bijection.core.object :refer :all]
            [locus.elementary.diamond.core.object :refer :all])
  (:import [locus.elementary.function.core.object SetFunction]
           [locus.elementary.diamond.core.object Diamond]
           [locus.elementary.bijection.core.object Bijection]))

; Objects in the topos Sets^[2,2]
; A gem is an object of the topos Sets^[2,2] and therefore it is a type of copresheaf.
; The data of a gem is specified by a pair of two different diamonds: one for the
; forwards function and one for the target function for a pair of bijections. So
; a gem is an extension of the concept of a diamond, hence their name. This file
; implements products, coproducts, subobjects, and quotients in the topos of gems.

; Implementation of gems
(deftype Gem [source-bijection target-bijection input-function output-function]
  AbstractMorphism
  (source-object [this] source-bijection)
  (target-object [this] target-bijection)

  ConcreteHigherMorphism
  (underlying-morphism-of-functions [this]
    (Diamond.
      (underlying-function source-bijection)
      (underlying-function target-bijection)
      input-function
      output-function))

  StructuredDifunction
  (first-function [this] input-function)
  (second-function [this] output-function))

; Composition and identities in the topos of bijections
(defmethod compose* Gem
  [a b]

  (Gem.
    (source-object b)
    (target-object a)
    (compose-functions (first-function a) (first-function b))
    (compose-functions (second-function a) (second-function b))))

(defn identity-gem
  [bijection]

  (Gem.
    bijection
    bijection
    (identity-function (inputs bijection))
    (identity-function (outputs bijection))))

(defmethod identity-morphism Bijection
  [func] (identity-gem func))

; Subobject classifier for the topos of bijections
; The topos of bijections is boolean like the topos of sets
(def truth-bijection
  (Bijection.
    #{false true}
    #{false true}
    identity
    identity))

(defn subbijection-character
  [bijection new-in new-out]

  (Gem.
    bijection
    truth-bijection
    (subset-character new-in (inputs bijection))
    (subset-character new-out (outputs bijection))))

; Relevant components
(defn subbijection-component
  [morphism]

  (list (function-image (first-function morphism))
        (function-image (second-function morphism))))

(defn bijection-congruence-component
  [morphism]

  (list (function-kernel (first-function morphism))
        (function-kernel (second-function morphism))))

(defn factorise-bijection-morphism
  [morphism]

  (list (bijection-congruence-component morphism)
        (subbijection-component morphism)))

; Inclusion and quotient gems
(defn inclusion-gem
  [bijection new-in new-out]

  (Gem.
    (restrict-bijection bijection new-in)
    bijection
    (inclusion-function new-in (inputs bijection))
    (inclusion-function new-out (outputs bijection))))

(defn projection-gem
  [f in-partition out-partition]

  (Gem.
    f
    (quotient-bijection f in-partition out-partition)
    (projection-function in-partition)
    (projection-function out-partition)))

(defn element-gem
  [f x]

  (let [out (f x)]
    (Gem.
      (pair-bijection x out)
      f
      (inclusion-function #{x} (inputs f))
      (inclusion-function #{out} (outputs f)))))

; Components of morphisms of bijections
; There is a key relationship between bijection morphisms and relation mappings
(defn interrelational-component
  [morphism]

  (SetFunction.
    (underlying-relation (source-object morphism))
    (underlying-relation (target-object morphism))
    (fn [[a b]]
      (list ((first-function morphism) a)
            ((second-function morphism) b)))))

(defn interrelational-morphism
  ([func]
   (interrelational-morphism
     (relation-to-bijection (set (keys func)))
     (relation-to-bijection (set (vals func)))
     func))
  ([source target func]
   (let [pairs (seq func)]
     (Gem.
       source
       target
       (relation-to-function
         (set
           (map
             (fn [[a b]]
               (list (first a) (first b)))
             pairs)))
       (relation-to-function
         (set
           (map
             (fn [[a b]]
               (list (second a) (second b)))
             pairs)))))))

; Products and coproducts in the topos of gems
(defmethod product Gem
  [& diamonds]

  (Gem.
    (apply product (map source-object diamonds))
    (apply product (map target-object diamonds))
    (apply product (map input-set-function diamonds))
    (apply product (map output-set-function diamonds))))

(defmethod coproduct Gem
  [& diamonds]

  (Gem.
    (apply coproduct (map source-object diamonds))
    (apply coproduct (map target-object diamonds))
    (apply coproduct (map input-set-function diamonds))
    (apply coproduct (map output-set-function diamonds))))

; Subobjects of gems
(defn subgem?
  [gem [first-new-in first-new-out] [second-new-in second-new-out]]

  (and
    (bijection-subobject? (first-function gem) first-new-in first-new-out)
    (bijection-subobject? (second-function gem) second-new-in second-new-out)
    (subfunction? (input-set-function gem) first-new-in second-new-in)
    (subfunction? (output-set-function gem) first-new-out second-new-out)))

(defn subgem
  [gem [first-new-in first-new-out] [second-new-in second-new-out]]

  (Gem.
    (subbijection (first-function gem) first-new-in first-new-out)
    (subbijection (second-function gem) second-new-in second-new-out)
    (subfunction (input-set-function gem) first-new-in second-new-in)
    (subfunction (output-set-function gem) first-new-out second-new-out)))

; Quotients of gems
(defn gem-congruence?
  [gem [in-partition1 out-partition1] [in-partition2 out-partition2]]

  (and
    (bijection-congruence? (first-function gem) in-partition1 out-partition1)
    (bijection-congruence? (second-function gem) in-partition2 out-partition2)
    (io-relation? (input-set-function gem) in-partition1 in-partition2)
    (io-relation? (output-set-function gem) out-partition1 out-partition2)))

(defn quotient-gem
  [gem [in-partition1 out-partition1] [in-partition2 out-partition2]]

  (Diamond.
    (quotient-bijection (first-function gem) in-partition1 out-partition1)
    (quotient-bijection (second-function gem) in-partition2 out-partition2)
    (quotient-function (input-set-function gem) in-partition1 in-partition2)
    (quotient-function (output-set-function gem) out-partition1 out-partition2)))

; Ontology of morphisms of gems
(defn gem?
  [ob]

  (= (type ob) Gem))

(defn monogem?
  [ob]

  (and
    (gem? ob)
    (injective? (first-function ob))
    (injective? (second-function ob))))

(defn epigem?
  [ob]

  (and
    (gem? ob)
    (surjective? (first-function ob))
    (surjective? (second-function ob))))

(defn isogem?
  [ob]

  (and
    (gem? ob)
    (invertible? (first-function ob))
    (invertible? (second-function ob))))

(defn endogem?
  [ob]

  (and
    (gem? ob)
    (equal-bijections? (source-object ob) (target-object ob))))

(defn autogem?
  [ob]

  (and
    (isogem? ob)
    (equal-bijections? (source-object ob) (target-object ob))))

(defn inclusion-gem?
  [ob]

  (and
    (gem? ob)
    (identity-function? (first-function ob))
    (identity-function? (second-function ob))))

(defn element-gem?
  [ob]

  (and
    (gem? ob)
    (size-one-bijection? (source-object ob))))

; Ontology of properties of gems
(defn !=gem
  [a b]

  (and
    (gem? a)
    (gem? b)
    (not= a b)))

(defn !=gem-source
  [a b]

  (and
    (gem? a)
    (gem? b)
    (not= (source-object a) (source-object b))))

(defn !=gem-target
  [a b]

  (and
    (gem? a)
    (gem? b)
    (not= (target-object a) (target-object b))))

(defn !=gem-input-function
  [a b]

  (and
    (gem? a)
    (gem? b)
    (not= (first-function a) (first-function b))))

(defn !=gem-output-function
  [a b]

  (and
    (gem? a)
    (gem? b)
    (not= (second-function a) (second-function b))))