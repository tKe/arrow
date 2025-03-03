@file:Suppress("CONTEXT_RECEIVERS_DEPRECATED")

package arrow.raise.ktor.server.request

import arrow.core.raise.Raise
import arrow.core.raise.withError
import arrow.raise.ktor.server.RaiseRoutingContext
import arrow.raise.ktor.server.Response
import io.ktor.server.routing.RoutingCall
import io.ktor.util.reflect.typeInfo
import kotlin.jvm.JvmName

@JvmName("pathOrRaiseReified")
public inline fun <reified A : Any> Raise<RequestError>.pathOrRaise(call: RoutingCall, name: String): A = parameterOrRaise(call.pathParameters, Parameter.Path(name), typeInfo<A>())
public inline fun <A : Any> Raise<RequestError>.pathOrRaise(call: RoutingCall, name: String, transform: Raise<String>.(String) -> A): A = parameterOrRaise(call.pathParameters, Parameter.Path(name), transform)
public fun Raise<RequestError>.pathOrRaise(call: RoutingCall, name: String): String = parameterOrRaise(call.pathParameters, Parameter.Path(name))

// RaiseRoutingContext (default error response)
@JvmName("pathOrRaiseReified")
public inline fun <reified A : Any> RaiseRoutingContext.pathOrRaise(name: String): A = errorRaise.pathOrRaise<A>(call, name)
public inline fun <A : Any> RaiseRoutingContext.pathOrRaise(name: String, transform: Raise<String>.(String) -> A): A = errorRaise.pathOrRaise(call, name, transform)
public fun RaiseRoutingContext.pathOrRaise(name: String): String = errorRaise.pathOrRaise(call, name)

// RaiseRoutingContext (custom error response)
@JvmName("pathOrRaiseReified")
public inline fun <reified A : Any> RaiseRoutingContext.pathOrRaise(name: String, errorResponse: (RequestError) -> Response): A = withError(errorResponse) { pathOrRaise<A>(call, name) }
public inline fun <A : Any> RaiseRoutingContext.pathOrRaise(name: String, errorResponse: (RequestError) -> Response, transform: Raise<String>.(String) -> A): A = withError(errorResponse) { pathOrRaise(call, name, transform) }
public inline fun RaiseRoutingContext.pathOrRaise(name: String, errorResponse: (RequestError) -> Response): String = withError(errorResponse) { pathOrRaise(call, name) }
