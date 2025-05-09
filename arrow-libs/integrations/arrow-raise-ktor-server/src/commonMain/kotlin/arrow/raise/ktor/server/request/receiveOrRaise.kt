@file:JvmName("RequestRaise")
@file:JvmMultifileClass
@file:OptIn(ExperimentalContracts::class)

package arrow.raise.ktor.server.request

import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.raise.ktor.server.Response
import arrow.raise.ktor.server.raiseError
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
public suspend inline fun <reified A : Any> receiveOrRaise(call: RoutingCall): A =
  call.receiveOrRaise(typeInfo<A>())

context(r: Raise<RequestError>)
public suspend inline fun <reified A : Any> receiveNullableOrRaise(call: RoutingCall): A? =
  call.receiveNullableOrRaise(typeInfo<A>())

context(r: Raise<Response>)
public suspend inline fun <reified A : Any> RoutingContext.receiveOrRaise(): A =
  raiseError { call.receiveOrRaise(typeInfo<A>()) }

context(r: Raise<Response>)
public suspend inline fun <reified A : Any> RoutingContext.receiveNullableOrRaise(): A? =
  raiseError { call.receiveNullableOrRaise(typeInfo<A>()) }

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
