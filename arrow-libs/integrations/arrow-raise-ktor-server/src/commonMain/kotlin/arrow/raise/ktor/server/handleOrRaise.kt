package arrow.raise.ktor.server

import arrow.core.raise.Raise
import arrow.core.raise.RaiseDSL
import arrow.core.raise.recover
import arrow.raise.ktor.server.Response.Companion.Response
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlin.jvm.JvmName

public typealias RaiseRoutingHandler = suspend context(Raise<Response>) RoutingContext.() -> Unit

@KtorDsl
@RaiseDSL
public fun Route.handleOrRaise(body: RaiseRoutingHandler): Unit = handle { handleOrRaise { body() } }

@PublishedApi
internal suspend inline fun RoutingContext.handleOrRaise(body: RaiseRoutingHandler): Unit =
  @Suppress("RemoveExplicitTypeArguments")
  recover<Response, _>({ body() }) { it.respondTo(call) }

@RaiseDSL
public fun Raise<Response>.raise(outgoingContent: OutgoingContent): Nothing = raise(Response(outgoingContent))

@RaiseDSL
public fun Raise<Response>.raise(statusCode: HttpStatusCode): Nothing = raise(Response(statusCode))

@RaiseDSL
public inline fun <reified T> Raise<Response>.raise(statusCode: HttpStatusCode, content: T): Nothing = raise(Response(statusCode, content))

context(r: Raise<Response>)
@RaiseDSL
@JvmName("raiseOutgoingContent")
public fun raise(outgoingContent: OutgoingContent): Nothing = r.raise(Response(outgoingContent))

context(r: Raise<Response>)
@RaiseDSL
@JvmName("raiseStatusCode")
public fun raise(statusCode: HttpStatusCode): Nothing = r.raise(Response(statusCode))

context(r: Raise<Response>)
@RaiseDSL
@JvmName("raiseStatusCodeWithPayload")
public inline fun <reified T> raise(statusCode: HttpStatusCode, payload: T): Nothing = r.raise(Response(statusCode, payload))
