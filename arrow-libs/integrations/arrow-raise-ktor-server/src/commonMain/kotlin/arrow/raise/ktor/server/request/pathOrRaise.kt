@file:JvmName("RequestRaise")
@file:JvmMultifileClass

package arrow.raise.ktor.server.request

import arrow.core.raise.Raise
import arrow.raise.ktor.server.Response
import arrow.raise.ktor.server.raisingErrorResponse
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

context(_: Raise<RequestError>)
@JvmName("pathOrRaiseReified")
public inline fun <reified A : Any> RoutingCall.pathOrRaise(name: String): A =
  pathOrRaise(name, typeInfo<A>())

context(_: Raise<RequestError>)
public inline fun <A : Any> RoutingCall.pathOrRaise(name: String, transform: ParameterTransform<A>): A =
  pathParameters.parameterOrRaise(Parameter.Path(name), transform)

context(_: Raise<RequestError>)
public fun RoutingCall.pathOrRaise(name: String): String =
  pathParameters.parameterOrRaise(Parameter.Path(name))

context(r: Raise<Response>)
@JvmName("pathOrRaiseReified")
public inline fun <reified A : Any> RoutingContext.pathOrRaise(name: String): A =
  raisingErrorResponse { call.pathOrRaise<A>(name) }

context(r: Raise<Response>)
public inline fun <A : Any> RoutingContext.pathOrRaise(name: String, transform: ParameterTransform<A>): A =
  raisingErrorResponse { call.pathOrRaise(name, transform) }

context(r: Raise<Response>)
public fun RoutingContext.pathOrRaise(name: String): String =
  raisingErrorResponse { call.pathOrRaise(name) }

context(c: RoutingContext)
@JvmName("pathOrRaiseReified")
public inline fun <reified A : Any> Raise<RequestError>.pathOrRaise(name: String): A =
  c.call.pathOrRaise<A>(name)

context(c: RoutingContext)
public inline fun <A : Any> Raise<RequestError>.pathOrRaise(name: String, transform: ParameterTransform<A>): A =
  c.call.pathOrRaise(name, transform)

context(c: RoutingContext)
public fun Raise<RequestError>.pathOrRaise(name: String): String =
  c.call.pathOrRaise(name)

context(_: Raise<RequestError>)
@PublishedApi
internal fun <A : Any> RoutingCall.pathOrRaise(name: String, typeInfo: TypeInfo): A =
  pathParameters.parameterOrRaise(Parameter.Path(name), typeInfo)
