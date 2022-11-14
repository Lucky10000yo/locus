(ns locus.elementary.lattice.combinat.impl
  (:require [locus.base.logic.core.set :refer :all]
            [locus.base.logic.limit.product :refer :all]
            [locus.base.logic.numeric.nms :refer :all]
            [locus.base.logic.numeric.sig :refer :all]
            [locus.base.partition.core.setpart :refer :all]
            [locus.base.function.core.object :refer :all]
            [locus.base.logic.structure.protocols :refer :all]
            [locus.elementary.copresheaf.core.protocols :refer :all]
            [locus.elementary.relation.binary.br :refer :all]
            [locus.elementary.relation.binary.sr :refer :all]
            [locus.elementary.relation.binary.product :refer :all]
            [locus.elementary.incidence.signatures.nf :refer :all]
            [locus.elementary.lattice.core.object :refer :all])
  (:import (locus.elementary.lattice.core.object Lattice)))

; Intervals lattice
(defn join-intervals*
  [pair1 pair2]

  (cond
    (empty? pair1) pair2
    (empty? pair2) pair1
    :else (let [[a b] pair1
                [c d] pair2]
            [(min a c)
             (max b d)])))

(defn meet-intervals*
  [pair1 pair2]

  (cond
    (empty? pair1) []
    (empty? pair2) []
    :else (let [[a b] pair1
                [c d] pair2]
            (let [new-start (max a c)
                  new-end (min b d)]
              (if (<= new-start new-end)
                [new-start new-end]
                [])))))

(defn interval-lattice
  [n]

  (Lattice.
    (all-intervals n)
    (monoidalize join-intervals*)
    (monoidalize meet-intervals*)))

; Noncrossing partition lattice
(defn noncrossing-partition-lattice
  [n]

  (Lattice.
    (set
      (filter
        noncrossing-range-partition?
        (set-partitions (set (range n)))))
    join-noncrossing-partitions
    meet-set-partitions))

; Get a lattice from a Moore family and its closure operation
(defn moore-lattice
  [family]

  (Lattice.
    (dimembers family)
    (fn [& args]
      (cl family (apply union args)))
    intersection))

; Youngs lattice of additive partitions
(def youngs-lattice
  (Lattice.
    additive-partition?
    young-join
    young-meet))

; Compute the lattice of preorders of a set
(defn lattice-of-preorders
  [coll]

  (Lattice.
    (set
      (filter
        (fn [rel]
          (and
            (every?
              (fn [i]
                (contains? rel (list i i)))
              coll)
            (preorder? rel)))
        (power-set (cartesian-power coll 2))))
    (fn [& args]
      (cl transitive? (apply union args)))
    intersection))
