@file:JvmName("RequestRaise")
@file:JvmMultifileClass
@file:OptIn(ExperimentalContracts::class)

package arrow.raise.ktor.server.request

import arrow.core.raise.Raise
import arrow.core.raise.catch
import io.ktor.http.Parameters
import io.ktor.serialization.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

context(r: Raise<RequestError>)
public suspend inline fun <reified A : Any> RoutingCall.receiveOrRaise(): A =
  receiveOrRaise(typeInfo<A>())

context(r: Raise<RequestError>)
public suspend inline fun <reified A : Any> RoutingCall.receiveNullableOrRaise(): A? =
  receiveNullableOrRaise(typeInfo<A>())

context(r: Raise<RequestError>)
public suspend fun RoutingCall.formParameters(): RaisingParameters =
  RaisingParameters(this@formParameters, receiveOrRaise<Parameters>(), Parameter::Form)

context(r: Raise<Malformed>)
@PublishedApi
internal suspend fun <A : Any> RoutingCall.receiveOrRaise(
  typeInfo: TypeInfo,
): A = r.handleConversionError(typeInfo) { receive(it) }

context(r: Raise<Malformed>)
@PublishedApi
internal suspend fun <A : Any> RoutingCall.receiveNullableOrRaise(
  typeInfo: TypeInfo,
): A? = r.handleConversionError(typeInfo) { receiveNullable(it) }

private inline fun <A> Raise<Malformed>.handleConversionError(
  typeInfo: TypeInfo,
  receive: (TypeInfo) -> A
): A {
  contract { callsInPlace(receive, AT_MOST_ONCE) }
  return catch({ receive(typeInfo) }) {
    val cause = when (it) {
      is ContentTransformationException,
      is ContentConvertException -> it
      is BadRequestException -> it.findConvertException() ?: it
      else -> throw it
    }
    raise(Malformed(ReceiveBody, "could not be deserialized to ${typeInfo.simpleName}", cause))
  }
}

private tailrec fun Throwable.findConvertException(): ContentConvertException? = when (this) {
  is ContentConvertException -> this
  else -> cause?.findConvertException()
}
