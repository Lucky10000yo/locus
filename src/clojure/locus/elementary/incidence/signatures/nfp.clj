(ns locus.elementary.incidence.signatures.nfp
  (:require [locus.elementary.logic.base.core :refer :all]
            [locus.elementary.logic.base.ap :refer :all]
            [locus.elementary.relation.binary.product :refer :all]
            [locus.elementary.incidence.signatures.nf :refer :all]))

; Let N be a natural number, then N can be associated to a set of additive partitions. An additive partition
; consists of natural numbers, which can be further partitioned into other additive partitions. This file
; provides support for such nested additive partitions. This is an area of continuing research.

(defn nested-additive-partitions
  [n]

  (mapcat
    (fn [partition]
      (apply
        cartesian-product
        (map
          (fn [i]
            (set (all-partitions i)))
          partition)))
    (all-partitions n)))


