package arrow.raise.ktor.server.delegation

import arrow.core.raise.Raise
import arrow.core.raise.recover
import arrow.raise.ktor.server.request.pathRaising
import arrow.raise.ktor.server.request.Malformed
import arrow.raise.ktor.server.request.MissingParameter
import arrow.raise.ktor.server.request.Parameter
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PathRaisingTest {
  @Test
  fun `raise on missing path parameter as implicit String`() {
    withRoutingContext("", "") {
      val raised = shouldRaise {
        val paramName by call.pathRaising()
        fail("should have raised already")
      }
      assertEquals(MissingParameter(Parameter.Path("paramName")), raised)
    }
  }

  @Test
  fun `doesn't raise on matching path parameter as String`() {
    withRoutingContext("/{present}", "/hello") {
      val result = shouldNotRaise {
        val present: String by call.pathRaising()
        present
      }
      assertEquals("hello", result)
    }
  }

  @Test
  fun `raise malformed on transform raise`() {
    withRoutingContext("/{present}", "/hello") {
      val raised = shouldRaise {
        val present by call.pathRaising { it.toIntOrNull() ?: raise("not a number") }
        fail("should have raised already")
      }
      assertEquals(Malformed(Parameter.Path("present"), "not a number"), raised)
    }
  }

  @Test
  fun `transformed path parameter`() {
    withRoutingContext("/{present}", "/123") {
      val result = shouldNotRaise {
        val present by call.pathRaising { it.toIntOrNull() ?: raise("not a number") }
        present
      }
      assertEquals(123, result)
    }
  }

  @Test
  fun `success across all options`() {
    withRoutingContext("/{a}/{b}/{c}", "/user/123/status") {
      shouldNotRaise {
        val a by call.pathRaising()
        val b by call.pathRaising<Long>()
        val c by call.pathRaising { it.uppercase() }

        assertEquals(a, "user")
        assertEquals(b, 123L)
        assertEquals(c, "STATUS")
      }
    }
  }

  @Test
  fun `success across all options inferred`() {
    withRoutingContext("/{a}/{b}/{c}", "/user/123/status") {
      shouldNotRaise {
        val a: String by call.pathRaising()
        val b: Long by call.pathRaising<_>()
        val c: String by call.pathRaising { it.uppercase() }

        assertEquals(a, "user")
        assertEquals(b, 123L)
        assertEquals(c, "STATUS")
      }
    }
  }

  fun <R> withRoutingContext(
    route: String,
    url: String,
    queryParameters: Parameters = parameters { },
    f: RoutingContext.() -> R
  ) = runTest {
    val routeContext = CompletableDeferred<RoutingContext>(coroutineContext.job)
    val app = TestApplication {
      routing {
        route(route) {
          handle {
            check(routeContext.complete(this))
            awaitCancellation()
          }
        }
      }
    }
    launch {app.client.get(url) { this.url.parameters.appendAll(queryParameters) }}
    try { f(routeContext.await()) }
    finally { app.stop() }
  }
}

private inline fun <Error> shouldRaise(f: Raise<Error>.() -> Any): Error =
  recover({
    val result = f(this@recover)
    fail("expected raise but returned $result")
  }, { it })

private inline fun <Error, R> shouldNotRaise(f: Raise<Error>.() -> R): R =
  recover(f, { fail("unexpected raise of $it") })
