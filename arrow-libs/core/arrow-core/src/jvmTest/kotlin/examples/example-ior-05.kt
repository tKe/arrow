// This file was automatically generated from Ior.kt by Knit tool. Do not edit.
package arrow.core.examples.exampleIor05

import arrow.core.Ior

fun main() {
  Ior.Right(12).mapLeft { "flower" } // Result: Right(12)
  Ior.Left(12).mapLeft { "flower" }  // Result: Left("power")
  Ior.Both(12, "power").mapLeft { "flower $it" }  // Result: Both("flower 12", "power")
}
