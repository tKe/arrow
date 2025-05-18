package arrow.raise.ktor.server.request

import arrow.core.raise.Raise
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.reflect.*

public class RaisingParameters internal constructor(
  @PublishedApi internal val call: ApplicationCall,
  private val parameters: Parameters,
  @PublishedApi
  internal val parameter: (String) -> Parameter
) : Parameters by parameters {
  context(r: Raise<RequestError>)
  public operator fun invoke(name: String): String = parameterOrRaise(parameter(name))

  context(r: Raise<RequestError>)
  public inline operator fun <reified T : Any> invoke(name: String): T = parameterOrRaise(parameter(name), typeInfo<T>())

  context(r: Raise<RequestError>)
  public inline operator fun <T : Any> invoke(name: String, transform: ParameterTransform<T>): T = parameterOrRaise(parameter(name), transform)

  context(r: Raise<RequestError>)
  public inline operator fun <reified T : Any> get(name: String): T = invoke<T>(name)

  context(r: Raise<RequestError>)
  public operator fun invoke(): RaisingProvider<String> =
    provider { Value(invoke(it)) }

  context(r: Raise<RequestError>)
  public inline operator fun <reified T : Any> invoke(marker: Unit = Unit): RaisingProvider<T> =
    provider { Value(invoke<T>(it)) }

  context(r: Raise<RequestError>)
  public inline operator fun <T : Any> invoke(crossinline transform: ParameterTransform<T>): RaisingProvider<T> =
    provider { Value(invoke(it, transform)) }
}

private val pathRaisingKey = AttributeKey<RaisingParameters>("pathRaising")
public val RoutingCall.pathRaising: RaisingParameters
  get() = attributes.computeIfAbsent(pathRaisingKey) {
    RaisingParameters(this, pathParameters, Parameter::Path)
  }

private val queryRaisingKey = AttributeKey<RaisingParameters>("queryRaising")
public val RoutingCall.queryRaising: RaisingParameters
  get() = attributes.computeIfAbsent(queryRaisingKey) {
    RaisingParameters(this, queryParameters, Parameter::Query)
  }
