package arrow.raise.ktor.server.response

import arrow.core.raise.Raise
import arrow.core.raise.RaiseDSL
import arrow.core.raise.recover
import arrow.core.raise.withError
import arrow.raise.ktor.server.response.Response.Companion.Response
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.util.*
import io.ktor.util.reflect.*


private val raiseKey: AttributeKey<Raise<Response>> = AttributeKey("KtorRaiseResponseRaise")

public val KtorRaise: RouteScopedPlugin<Unit> = createRouteScopedPlugin("KtorRaiseResponse") {
  (route ?: application).intercept(ApplicationCallPipeline.Plugins) {
    recover({
      context.attributes.put(raiseKey, this)
      try {
        proceed()
      } finally {
        context.attributes.remove(raiseKey)
      }
    }, { response: Response ->
      if (!context.isHandled) response.respondTo(context)
    })
  }
}

@PublishedApi
internal val ApplicationCall.raise: Raise<Response> get() = attributes[raiseKey]

public suspend inline fun <reified TResponse> ApplicationCall.respondOrRaise(
  statusCode: HttpStatusCode? = null,
  body: suspend Raise<Response>.() -> TResponse,
): Nothing = respondOrRaise(statusCode, typeInfo<TResponse>(), body)

public suspend inline fun <Error, reified TResponse> ApplicationCall.respondOrRaise(
  statusCode: HttpStatusCode? = null,
  errorResponse: (Error) -> Response,
  body: suspend Raise<Error>.() -> TResponse,
): Nothing = respondOrRaise(statusCode, typeInfo<TResponse>()) { withError(errorResponse) { body(this) } }

public suspend inline fun <Error, reified TResponse> ApplicationCall.respondOrRaise(
  errorResponse: (Error) -> Response,
  statusCode: HttpStatusCode? = null,
  body: suspend Raise<Error>.() -> TResponse,
): Nothing = respondOrRaise(statusCode, typeInfo<TResponse>()) { withError(errorResponse) { body(this) } }

@PublishedApi
internal suspend inline fun <TResponse> ApplicationCall.respondOrRaise(
  statusCode: HttpStatusCode? = null,
  responseTypeInfo: TypeInfo,
  body: suspend Raise<Response>.() -> TResponse,
): Nothing = raise.raise(raise.body().toResponse(statusCode, responseTypeInfo))

@PublishedApi
internal fun Any?.toResponse(statusCode: HttpStatusCode?, typeInfo: TypeInfo): Response = when (this) {
  is Unit -> Response(statusCode ?: HttpStatusCode.NoContent)
  is HttpStatusCode -> Response(this)
  is Response -> this
  else -> Response(statusCode ?: HttpStatusCode.OK, this, typeInfo)
}

@RaiseDSL
public fun Raise<Response>.raise(outgoingContent: OutgoingContent): Nothing = raise(Response(outgoingContent))

@RaiseDSL
public fun Raise<Response>.raise(statusCode: HttpStatusCode): Nothing = raise(Response(statusCode))

@RaiseDSL
public inline fun <reified T> Raise<Response>.raise(statusCode: HttpStatusCode, content: T): Nothing = raise(Response(statusCode, content))
