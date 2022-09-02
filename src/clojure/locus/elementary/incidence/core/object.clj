(ns locus.elementary.incidence.core.object
  (:require [locus.elementary.logic.base.core :refer :all]
            [locus.elementary.logic.order.seq :refer :all]
            [locus.elementary.relation.binary.br :refer :all]
            [locus.elementary.relation.binary.sr :refer :all]
            [locus.elementary.relation.binary.product :refer :all]
            [locus.elementary.function.core.object :refer :all]
            [locus.elementary.function.core.protocols :refer :all]
            [locus.elementary.function.core.util :refer :all]
            [locus.elementary.diset.core.object :refer :all]
            [locus.elementary.bijection.core.object :refer :all])
  (:import (locus.elementary.function.core.object SetFunction)
           (locus.elementary.relation.binary.sr SeqableRelation)))

; Objects in the topos Sets^[1,2]
; Span copresheaves can be used to define generalisations of hypergraphs, that include repeated
; edges and repeated members of edges. In particular, a span copresheaf is defined by a pair
; of functions on a common set of flags to a set of vertices and a set of edges. Two different
; flags may map to the same pair of vertices and edges, indicating repeated membership of a
; vertex within an edge. In order to get around this we can get the together injective
; quotient of a span copresheaf, which always avoids repeated membership of edges
; within vertices.
(deftype Span [flags edges vertices efn vfn])

(defn span
  [f g]

  (let [a (inputs f)
        b (outputs f)
        c (outputs g)]
    (Span. a b c f g)))

; Components of the span copresheaf
(defn span-flags
  [^Span span]

  (.flags span))

(defn span-vertices
  [^Span span]

  (.vertices span))

(defn span-edges
  [^Span span]

  (.edges span))

(defn edge-fn
  [^Span span]

  (.efn span))

(defn vertex-fn
  [^Span span]

  (.vfn span))

(defn edge-component
  [^Span span, flag]

  ((.efn span) flag))

(defn vertex-component
  [^Span span, flag]

  ((.vfn span) flag))

(defn edge-function
  [^Span span]

  (SetFunction.
    (span-flags span)
    (span-edges span)
    (fn [flag]
      (edge-component span flag))))

(defn vertex-function
  [^Span span]

  (SetFunction.
    (span-flags span)
    (span-edges span)
    (fn [flag]
      (vertex-component span flag))))

; Numeric properties of spans
(def span-order
  (comp count span-vertices))

(def span-size
  (comp count span-edges))

(def span-cardinality-sum
  (comp count span-flags))

; Properties of edges
(defn get-edge-flags
  [^Span span, edge]

  (let [efn (edge-fn span)]
    (filter
      (fn [flag]
        (= (efn flag) edge))
      (span-flags span))))

(defn get-incident-vertices
  [^Span span, edge]

  (let [vfn (vertex-fn span)]
    (multiset
      (map
        (fn [flag]
          (vfn flag))
        (get-edge-flags span edge)))))

(defn edge-size
  [^Span span, edge]

  (count (get-incident-vertices span edge)))

(defn edge-signature
  [^Span span, edge]

  (signature (get-incident-vertices span edge)))

(defn setlike-edge?
  [^Span span, edge]

  (let [coll (get-incident-vertices span edge)]
    (= (count coll) (count (support coll)))))

(defn complete-setlike-edge?
  [^Span span, edge]

  (let [coll (get-incident-vertices span edge)]
    (and
      (universal? coll)
      (= (count coll) (span-order span)))))

(defn empty-edge?
  [^Span span, edge]

  (zero? (count (edge-size span edge))))

; Properties of vertices
(defn get-vertex-flags
  [^Span span, vertex]

  (let [vfn (vertex-fn span)]
    (filter
      (fn [flag]
        (= (vfn flag) vertex))
      (span-flags span))))

(defn get-incident-edges
  [^Span span, vertex]

  (let [efn (edge-fn span)]
    (multiset
      (map
        (fn [flag]
          (efn flag))
        (get-vertex-flags span vertex)))))

(defn vertex-degree
  [^Span span, vertex]

  (count (get-incident-edges span vertex)))

(defn vertex-signature
  [^Span span, vertex]

  (signature (get-incident-edges span vertex)))

(defn empty-vertex?
  [^Span span, vertex]

  (zero? (count (vertex-degree span vertex))))

; Properties of flags
(defn flag-pair
  [^Span span, flag]

  (list
    (edge-component span flag)
    (vertex-component span flag)))

; Enumerate special types of edges
(defn empty-edges
  [^Span span]

  (set
    (filter
      (fn [edge]
        (zero? (edge-size span edge)))
      (span-edges span))))

; Enumerate special types of vertices
(defn empty-vertices
  [^Span span]

  (set
    (filter
      (fn [vertex]
        (zero? (vertex-degree span vertex)))
      (span-vertices span))))

; Enumerate special types of flags
(defn get-pair-flags
  [^Span span, vertex, edge]

  (let [efn (edge-fn span)
        vfn (vertex-fn span)]
    (filter
      (fn [flag]
        (and
          (= (efn flag) edge)
          (= (vfn flag) vertex)))
      (span-flags span))))

; Vertex and edge preorders
(defn edge-preorder
  [span]

  (SeqableRelation.
    (span-edges span)
    (fn [[e1 e2]]
      (superbag? (list (get-incident-vertices span e1) (get-incident-vertices span e2))))
    {}))

(defn vertex-preorder
  [span]

  (SeqableRelation.
    (span-vertices span)
    (fn [[v1 v2]]
      (superbag? (list (get-incident-vertices span v1) (get-incident-vertices span v2))))
    {}))

; Get flag equality
(defn flag-partition
  [span]

  (pn
    (fn [a b]
      (= (flag-pair span a)
         (flag-pair span b)))
    (span-flags span)))

; Edge and vertex degree sequences
(defn vertex-degree-sequence
  [^Span span]

  (map
    (fn [vertex]
      (vertex-degree span vertex))
    (span-vertices span)))

(defn edge-size-sequence
  [^Span span]

  (map
    (fn [edge]
      (edge-size span edge))
    (span-edges span)))

; Set systems and multiset systems related to incidence
(defn vertex-system
  [^Span span]

  (multiset
    (map
      (fn [edge]
        (get-incident-vertices span edge))
      (span-edges span))))

(defn edge-system
  [^Span span]

  (multiset
    (map
      (fn [vertex]
        (get-incident-edges span vertex))
      (span-vertices span))))

; Relations related to incidence structures
(defn flag-pairs
  [span]

  (multiset
    (map
      (fn [i]
        (flag-pair span i))
      (span-flags span))))

(defn incidence-relation
  [span]

  (set (flag-pairs span)))

(defmethod underlying-relation Span
  [^Span span]

  (incidence-relation span))

(defmethod underlying-multirelation Span
  [^Span span]

  (flag-pairs span))

; Duality in the incidence topos
(defn dual-span
  [^Span span]

  (Span.
    (span-flags span)
    (span-vertices span)
    (span-edges span)
    (vertex-fn span)
    (edge-fn span)))

; Create a span matrix
(defn initialize-matrix
  [height width]

  (vec
    (map
      (fn [i]
        (vec (repeat width 0)))
      (range height))))

(defn increment-matrix-index
  [matrix y x]

  (assoc-in matrix [y x] (inc (get-in matrix [y x]))))

(defn create-labeling
  [coll]

  (let [ordered-coll (seq coll)]
    (zipmap
      ordered-coll
      (range (count ordered-coll)))))

(defn span-matrix
  [^Span span]

  (let [el (create-labeling (span-edges span))
        vl (create-labeling (span-vertices span))
        initial-matrix (initialize-matrix
                         (count (span-edges span))
                         (count (span-vertices span)))
        flags (seq (span-flags span))]
    (loop [current-flags flags
           current-matrix initial-matrix]
      (if (empty? current-flags)
        current-matrix
        (let [current-element (first current-flags)
              y (el (edge-component span current-element))
              x (vl (vertex-component span current-element))]
          (recur
            (rest current-flags)
            (increment-matrix-index current-matrix y x)))))))

(defn display-span-matrix
  [^Span span]

  (let [coll (span-matrix span)]
    (doseq [i coll]
      (prn i))))

; A simple span is one for which doesn't have any flags with equal decompositions
; In order to define this we of course use the traditional (edge,vertex) ordering
; of flag decompositions in the span topos.
(defn simple-span
  [edges vertices rel]

  (Span.
    rel
    edges
    vertices
    first
    second))

(defn family-span
  [edges]

  (let [vertices (dimembers edges)
        rel (seqable-binary-relation
              edges
              vertices
              (fn [[edge vertex]]
                (contains? edge vertex)))]
    (simple-span
      edges
      vertices
      rel)))

(defn multiplicity-relation
  [coll]

  (set
    (mapcat
      (fn [elem]
        (map
          (fn [i]
            (list elem i))
          (range (multiplicity coll elem))))
      (support coll))))

(defn multifamily-span
  [coll]

  (let [vertices (dimembers coll)
        edges (multiplicity-relation coll)
        rel (seqable-binary-relation
              edges
              vertices
              (fn [[[edge n] vertex]]
                (contains? edge vertex)))]
    (simple-span
      edges
      vertices
      rel)))

; Complex spans, meaning ones that are not simple, can have the property
; that a given pair of an incident vertex and an edge can occur more then
; once. These are an important part of the complete theory of the topos
; of span copresheaves.
(defn multiset-classifier
  [coll]

  (apply
    union
    (map
      (fn [elem]
        (set
          (map
            (fn [i]
              (list coll elem i))
            (range (multiplicity coll elem)))))
      (support coll))))

(defn clan-span
  [coll]

  (let [vertices (apply union (map set coll))]
    (->Span
      (apply union (map multiset-classifier coll))
      coll
      vertices
      first
      second)))

(defn multiclan-span
  [coll]

  (let [edges (multiplicity-relation coll)
        vertices (apply union (map set coll))]
    (->Span
      (apply
        union
        (map
          (fn [[edge n]]
            (apply
              union
              (map
                (fn [elem]
                  (set
                    (map
                      (fn [i]
                        (list (list edge n) elem i))
                      (range (multiplicity edge elem)))))
                (support edge))))
          edges))
      edges
      vertices
      first
      second)))

; Product span
; A product span is created by getting the pair of projection functions
; from the product and using them as edge and vertex functions.
(defn product-span
  [a b]

  (Span.
    (cartesian-product a b)
    a
    b
    first
    second))

; Subobjects of spans
(defn subspan
  [span new-flags new-edges new-vertices]

  (Span.
    new-flags
    new-edges
    new-vertices
    (edge-function span)
    (vertex-function span)))

(defn restrict-flags
  [span new-flags]

  (Span.
    new-flags
    (span-edges span)
    (span-vertices span)
    (edge-function span)
    (vertex-function span)))

(defn subspan?
  [span new-flags new-edges new-vertices]

  (let [efn (edge-function span)
        vfn (vertex-function span)]
    (and
      (superset?
        (list (set-image efn new-flags) new-edges))
      (superset?
        (list (set-image vfn new-flags) new-vertices)))))

; Quotients in the topos of span copresheaves
(defn span-congruence?
  [span flag-partition edge-partition vertex-partition]

  (let [efn (edge-function span)
        vfn (vertex-function span)]
    (and
      (io-relation? efn flag-partition edge-partition)
      (io-relation? vfn flag-partition vertex-partition))))

(defn quotient-span
  [span flag-partition edge-partition vertex-partition]

  (let [efn (edge-function span)
        vfn (vertex-function span)]
    (span
      (quotient-function
        efn
        flag-partition
        edge-partition)
      (quotient-function
        vfn
        flag-partition
        vertex-partition))))

(defn together-injective-quotient
  [span]

  (restrict-flags
    span
    (set
      (map
        (fn [flag-set]
          (first flag-set))
        (flag-partition span)))))

; Products and coproducts in the topos of span copresheaves
(defn span-product
  [& spans]

  (Span.
    (apply product (map span-flags spans))
    (apply product (map span-edges spans))
    (apply product (map span-vertices spans))
    (fn [coll]
      (map-indexed
        (fn [i flag]
          (edge-component (nth spans i) flag))
        coll))
    (fn [coll]
      (map-indexed
        (fn [i flag]
          (vertex-component (nth spans i) flag))
        coll))))

(defn span-coproduct
  [& spans]

  (Span.
    (apply coproduct (map span-flags spans))
    (apply coproduct (map span-edges spans))
    (apply coproduct (map span-vertices spans))
    (fn [[i v]]
      (list i (edge-component (nth spans i) v)))
    (fn [[i v]]
      (list i (vertex-component (nth spans i) v)))))

(defmethod product Span
  [& args]

  (apply span-product args))

(defmethod coproduct Span
  [& args]

  (apply span-coproduct args))

; Ontology of span copresheaves
(defn span?
  [object]

  (= (type object) Span))

(defn flag-distinguishing-span?
  [span]

  (and
    (span? span)
    (loop [remaining-flags (seq (span-flags span))
           seen-flag-pairs #{}]
      (if (empty? remaining-flags)
        true
        (let [next-flag (first remaining-flags)
              next-flag-pair (flag-pair span next-flag)]
          (if (contains? seen-flag-pairs next-flag-pair)
            false
            (recur
              (rest remaining-flags)
              (conj seen-flag-pairs next-flag-pair))))))))

(defn edge-distinguishing-span?
  [span]

  (and
    (span? span)
    (loop [remaining-edges (seq (span-edges span))
           seen-edge-systems #{}]
      (if (empty? remaining-edges)
        true
        (let [next-edge (first remaining-edges)
              next-edge-system (get-incident-vertices span next-edge)]
          (if (contains? seen-edge-systems next-edge-system)
            false
            (recur
              (rest remaining-edges)
              (conj seen-edge-systems next-edge-system))))))))

(defn vertex-distinguishing-span?
  [span]

  (and
    (span? span)
    (loop [remaining-vertices (seq (span-vertices span))
           seen-vertex-systems #{}]
      (if (empty? remaining-vertices)
        true
        (let [next-vertex (first remaining-vertices)
              next-vertex-system (get-incident-edges span next-vertex)]
          (if (contains? seen-vertex-systems next-vertex-system)
            false
            (recur
              (rest remaining-vertices)
              (conj seen-vertex-systems next-vertex-system))))))))

; Uniform and regular span copresheaves
; A uniform span has edges all the same size and a regular span has vertices all the same degree
(defn uniform-span?
  [span]

  (and
    (span? span)
    (let [edges (seq (span-edges span))]
      (if (empty? edges)
        true
        (let [first-edge (first edges)
              first-cardinality (edge-size span first-edge)]
          (every?
            (fn [edge]
              (= first-cardinality (edge-size span edge)))
            edges))))))

(defn regular-span?
  [span]

  (and
    (span? span)
    (let [vertices (seq (span-vertices span))]
      (if (empty? vertices)
        true
        (let [first-vertex (first vertices)
              first-degree (vertex-degree span first-vertex)]
          (every?
            (fn [vertex]
              (= first-degree (vertex-degree span vertex)))
            vertices))))))

(defn two-regular-span?
  [span]

  (and
    (span? span)
    (let [vertices (seq (span-vertices span))]
      (every?
        (fn [vertex]
          (= 2 (vertex-degree span vertex)))
        vertices))))

(defn three-regular-span?
  [span]

  (and
    (span? span)
    (let [vertices (seq (span-vertices span))]
      (every?
        (fn [vertex]
          (= 3 (vertex-degree span vertex)))
        vertices))))

(defn four-regular-span?
  [span]

  (and
    (span? span)
    (let [vertices (seq (span-vertices span))]
      (every?
        (fn [vertex]
          (= 4 (vertex-degree span vertex)))
        vertices))))

; Classification of spans by edge size
(defn rank-four-span?
  [span]

  (and
    (span? span)
    (every?
      (fn [edge]
        (<= (edge-size span edge) 4))
      (span-edges span))))

(defn rank-three-span?
  [span]

  (and
    (span? span)
    (every?
      (fn [edge]
        (<= (edge-size span edge) 3))
      (span-edges span))))

(defn rank-two-span?
  [span]

  (and
    (span? span)
    (every?
      (fn [edge]
        (<= (edge-size span edge) 2))
      (span-edges span))))

(defn rank-one-span?
  [span]

  (and
    (span? span)
    (every?
      (fn [edge]
        (<= (edge-size span edge) 1))
      (span-edges span))))

; Unary, binary, ternary, and quaternary spans
(defn unary-span?
  [span]

  (and
    (span? span)
    (every?
      (fn [edge]
        (= (edge-size span edge) 1))
      (span-edges span))))

(defn binary-span?
  [span]

  (and
    (span? span)
    (every?
      (fn [edge]
        (= (edge-size span edge) 2))
      (span-edges span))))

(defn ternary-span?
  [span]

  (and
    (span? span)
    (every?
      (fn [edge]
        (= (edge-size span edge) 3))
      (span-edges span))))

(defn quaternary-span?
  [span]

  (and
    (span? span)
    (every?
      (fn [edge]
        (= (edge-size span edge) 4))
      (span-edges span))))

; Dual concepts for degrees of spans
(defn max-degree-four-span?
  [span]

  (and
    (span? span)
    (every?
      (fn [vertex]
        (<= (vertex-degree span vertex) 4))
      (span-vertices span))))

(defn max-degree-three-span?
  [span]

  (and
    (span? span)
    (every?
      (fn [vertex]
        (<= (vertex-degree span vertex) 3))
      (span-vertices span))))

(defn max-degree-two-span?
  [span]

  (and
    (span? span)
    (every?
      (fn [vertex]
        (<= (vertex-degree span vertex) 2))
      (span-vertices span))))

(defn max-degree-one-span?
  [span]

  (and
    (span? span)
    (every?
      (fn [vertex]
        (<= (vertex-degree span vertex) 1))
      (span-vertices span))))

; Check for the existence of empty vertices and edges
(defn nullfree-span?
  [span]

  (and
    (span? span)
    (empty? (empty-edges span))))

(defn vertex-surjective-span?
  [span]

  (and
    (span? span)
    (empty? (empty-vertices span))))

; Spans without vertices or edges
(defn edgefree-span?
  [span]

  (and
    (span? span)
    (empty? (span-edges span))))

(defn vertexfree-span?
  [span]

  (and
    (span? span)
    (empty? (span-vertices span))))

; A product span is constructed from complete setlike edges
(defn product-span?
  [span]

  (and
    (span? span)
    (every?
      (fn [edge]
        (complete-setlike-edge? span edge))
      (span-edges span))))



