@file:JvmName("RequestRaise")
@file:JvmMultifileClass
@file:OptIn(ExperimentalContracts::class)

package arrow.raise.ktor.server.request

import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.ensureNotNull
import arrow.core.raise.recover
import io.ktor.http.*
import io.ktor.util.converters.*
import io.ktor.util.reflect.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

public typealias ParameterTransform<O> = Raise<String>.(String) -> O

context(r: Raise<RequestError>)
@PublishedApi
internal inline fun <A : Any> Parameters.parameterOrRaise(
  parameter: Parameter,
  transform: ParameterTransform<A>,
): A {
  contract { callsInPlace(transform, AT_MOST_ONCE) }
  val value = parameterOrRaise(parameter)
  return recover({ transform(this, value) }) {
    r.raise(Malformed(parameter, it))
  }
}

context(r: Raise<MissingParameter>)
@PublishedApi
internal fun Parameters.parameterOrRaise(
  parameter: Parameter,
): String = r.ensureNotNull(get(parameter.name)) { MissingParameter(parameter) }

context(r: Raise<RequestError>)
@PublishedApi
internal fun <A : Any> Parameters.parameterOrRaise(
  parameter: Parameter,
  typeInfo: TypeInfo,
): A {
  // following what [Parameters.getOrFail] does...
  val values = r.ensureNotNull(getAll(parameter.name)) { MissingParameter(parameter) }
  return catch({
    @Suppress("UNCHECKED_CAST")
    DefaultConversionService.fromValues(values, typeInfo) as A
  }) {
    r.raise(
      Malformed(
        component = parameter,
        message = "couldn't be parsed/converted to ${typeInfo.simpleName}",
        cause = it,
      ),
    )
  }
}
