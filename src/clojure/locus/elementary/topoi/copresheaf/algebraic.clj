(ns locus.elementary.topoi.copresheaf.algebraic
  (:require [locus.elementary.logic.base.core :refer :all]
            [locus.elementary.function.core.object :refer :all]
            [locus.elementary.function.core.protocols :refer :all]
            [locus.elementary.topoi.copresheaf.object :refer :all]
            [locus.elementary.category.core.object :refer :all]
            [locus.hypergraph.core.object :refer :all]
            [locus.hypergraph.incidence.incidence-structure :refer :all]
            [locus.graph.undirected.object :refer :all]
            [locus.graph.directed.object :refer :all]
            [locus.algebra.magma.object :refer :all]
            [locus.algebra.pointed-set.object :refer :all]
            [locus.algebra.partial-magma.object :refer :all]
            [locus.ring.core.protocols :refer :all]
            [locus.semiring.core.object :refer :all])
  (:import (locus.elementary.topoi.copresheaf.object Copresheaf)
           (locus.algebra.magma.object Magma)
           (locus.algebra.pointed_set.object PointedSet)
           (locus.algebra.partial_magma.object PartialMagma)
           (locus.semiring.core.object Semiring)
           (locus.hypergraph.core.object Hypergraph)
           (locus.graph.undirected.object Graph)
           (locus.hypergraph.incidence.incidence_structure IncidenceStructure)
           (locus.graph.directed.object Digraph)))

; Graph category
(def graph-category
  (simple-labeled-category
    #{"edges" "vertices"}
    {"1ₑ" ["edges" "edges"]
     "1ᵥ" ["vertices" "vertices"]}))

(def incidence-structure-category
  (simple-labeled-category
    #{"points" "lines" "flags"}
    {"1ₚ" ["points" "points"]
     "1ₗ" ["lines" "lines"]
     "1f" ["flags" "flags"]}))

; Graphs as disets
; Even though they are not typically considered in these terms, it is possible to consider
; graphs and related structures in terms of topos of copresheaves over discrete categories,
; instead of in terms of the span category. Both approaches our supported here.
(defn graph-copresheaf
  [vertices edges]

  (Copresheaf.
    graph-category
    (fn [obj]
      (case obj
        "vertices" vertices
        "edges" edges))
    (fn [elem]
      (case elem
        "1ₑ" (identity-function edges)
        "1ᵥ" (identity-function vertices)))))

(defmethod to-copresheaf Graph
  [^Graph graph]

  (graph-copresheaf (.vertices graph) (.edges graph)))

(defmethod to-copresheaf Digraph
  [^Digraph digraph]

  (graph-copresheaf (underlying-set digraph) (underlying-relation digraph)))

(defmethod to-copresheaf Hypergraph
  [^Hypergraph graph]

  (graph-copresheaf (.vertices graph) (.edges graph)))

(defmethod to-copresheaf IncidenceStructure
  [^IncidenceStructure structure]

  (Copresheaf.
    incidence-structure-category
    (fn [obj]
      (case obj
        "points" (.points structure)
        "lines" (.lines structure)
        "flags" (.flags structure)))
    (fn [arrow]
      (case arrow
        "1ₚ" (identity-function (.points structure))
        "1ₗ" (identity-function (.lines structure))
        "1f" (identity-function (.flags structure))))))

; Copresheaf representation of functional algebras
(defmethod to-copresheaf Magma
  [magma]

  (to-copresheaf (underlying-function magma)))

(defmethod to-copresheaf PointedSet
  [pointed-set]

  (to-copresheaf (underlying-function pointed-set)))

(defmethod to-copresheaf PartialMagma
  [partial-magma]

  (to-copresheaf (underlying-function partial-magma)))

; Algebraic copresheaves defined by cospan copresheafs
(def subscript-digit-strings
  ["₀" "₁" "₂" "₃" "₄" "₅" "₆" "₇" "₈" "₉"])

(def ordinary-digit-strings
  ["0" "1" "2" "3" "4" "5" "6" "7" "8" "9"])

(defn subscriptize
  [num]

  (apply
    str
    (map
     (fn [char]
       (let [c (Integer/parseInt (.toString char))]
         (nth subscript-digit-strings c)))
     (seq num))))

(defn unsubscriptize
  [num]

  (let [unsubscriptize-mapping (zipmap subscript-digit-strings ordinary-digit-strings)]
    (apply
     str
     (map
       (fn [char]
         (get unsubscriptize-mapping (.toString char)))
       (seq num)))))

(defn algebraic-category
  [labels]

  (let [n (count labels)]
    (simple-labeled-category
      (conj (set (map str (range 1 (inc n)))) "S")
      (into
        {}
        (concat
          (list ["1ₛ" ["S" "S"]])
          (map
            (fn [i]
              (let [si (.toString i)]
                [(str "1" (subscriptize si)) [si si]]))
            (range 1 (inc n)))
          (map-indexed
            (fn [i label]
              [label [(str (inc i)) "S"]])
            labels))))))

(defn algebraic-copresheaf
  [labels functions]

  (when (not (empty? functions))
    (let [out (outputs (first functions))]
      (Copresheaf.
        (algebraic-category labels)
        (fn [obj]
          (if (= obj "S")
            out
            (let [n (dec (Integer/parseInt obj))]
              (inputs (nth functions n)))))
        (fn [arrow]
          (if (= arrow "1ₛ")
            (identity-function out)
            (if (and
                  (<= 2 (.length arrow))
                  (= (.toString (.charAt arrow 0)) "1"))
              (let [index-string (.substring arrow 1 (count arrow))
                    index (dec (Integer/parseInt (unsubscriptize index-string)))]
                (identity-function (inputs (nth functions index))))
              (nth functions (.indexOf labels arrow)))))))))

; Convert rings into copresheaves
(defmethod to-copresheaf Semiring
  [ring]

  (let [type (cond
               (ring-with-identity? ring) 0
               (ring? ring) 1
               (semiring-with-identity? ring) 2
               (semiring? ring) 3)]
    (case type
      0 (algebraic-copresheaf
          ["+" "-" "0" "*" "1"]
          [(addition-function ring)
           (additive-inverse-function ring)
           (additive-identity-function ring)
           (multiplication-function ring)
           (multiplicative-identity-function ring)])
      1 (algebraic-copresheaf
          ["+" "-" "0" "*"]
          [(addition-function ring)
           (additive-inverse-function ring)
           (additive-identity-function ring)
           (multiplication-function ring)])
      2 (algebraic-copresheaf
          ["+" "0" "*" "1"]
          [(addition-function ring)
           (additive-identity-function ring)
           (multiplication-function ring)
           (multiplicative-identity-function ring)])
      3 (algebraic-copresheaf
          ["+" "0" "*"]
          [(addition-function ring)
           (additive-identity-function ring)
           (multiplication-function ring)]))))


