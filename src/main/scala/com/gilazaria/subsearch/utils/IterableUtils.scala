package com.gilazaria.subsearch.utils

import com.gilazaria.subsearch.model.Record

import scala.collection.SortedSet

object IterableUtils {
  implicit class IterableImprovements(iterable: Iterable[Record]) {
    def toSortedSet: SortedSet[Record] = {
      SortedSet[Record]() ++ iterable
    }
  }
}
