package arrow.raise.ktor.server

import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.raise.ktor.server.request.RequestError
import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.RoutingContext

context(r: Raise<Response>)
@PublishedApi
internal inline fun <R> RoutingContext.raisingErrorResponse(f: Raise<RequestError>.() -> R): R = call.raisingErrorResponse(f)

context(r: Raise<Response>)
@PublishedApi
internal inline fun <R> RoutingContext.raisingErrorsResponse(f: Raise<NonEmptyList<RequestError>>.() -> R): R = call.raisingErrorsResponse(f)

context(r: Raise<Response>)
@PublishedApi
internal inline fun <R> ApplicationCall.raisingErrorResponse(f: Raise<RequestError>.() -> R): R = r.imap(::errorResponse).f()

context(r: Raise<Response>)
@PublishedApi
internal inline fun <R> ApplicationCall.raisingErrorsResponse(f: Raise<NonEmptyList<RequestError>>.() -> R): R = r.imap(::errorsResponse).f()

@PublishedApi
internal fun <Error, OtherError> Raise<Error>.imap(f: (OtherError) -> Error): Raise<OtherError> = object : Raise<OtherError> {
  override fun raise(r: OtherError): Nothing = raise(f(r))
}
