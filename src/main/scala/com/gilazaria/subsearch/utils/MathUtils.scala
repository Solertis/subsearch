package com.gilazaria.subsearch.utils

object MathUtils {
  def percentage(a: Int, b: Int): Float =
    if (b == 0) throw new IllegalArgumentException("The second argument cannot be zero.")
    else a.toFloat / b.toFloat * 100
}
