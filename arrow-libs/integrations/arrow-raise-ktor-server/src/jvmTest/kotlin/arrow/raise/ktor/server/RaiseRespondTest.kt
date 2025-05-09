package arrow.raise.ktor.server

import arrow.core.raise.Raise
import arrow.raise.ktor.server.Response.Companion.Response
import arrow.raise.ktor.server.Response.Companion.invoke
import arrow.raise.ktor.server.internal.ensureNotNull
import arrow.raise.ktor.server.internal.raise
import arrow.raise.ktor.server.internal.withError
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.serialization.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import kotlin.test.Test

class RaiseRespondTest {
  @Test
  fun `respondOrRaise nested in get`() = testApplication {
    routing {
      get("/foo") {
        respondOrRaise { "bar" }
      }
    }

    client.get("/foo").let {
      assertSoftly {
        it.status shouldBe OK
        it.bodyAsText() shouldBe "bar"
      }
    }
  }

  @Test
  fun `respondOrRaise Unit is NoContent`() = testApplication {
    routing {
      get("/foo") {
        respondOrRaise { }
      }
    }

    client.get("/foo").let {
      assertSoftly {
        it.status shouldBe HttpStatusCode.NoContent
        it.bodyAsText() shouldBe ""
      }
    }
  }

  @Test
  fun `respond result of getOrRaise`() = testApplication {
    routing {
      getOrRaise("/foo", HttpStatusCode.Created) { "bar" }
    }

    client.get("/foo").let {
      assertSoftly {
        it.status shouldBe HttpStatusCode.Created
        it.bodyAsText() shouldBe "bar"
      }
    }
  }

  @Test
  fun `respond with empty when getOrRaise returns status code`() = testApplication {
    routing {
      getOrRaise("/foo") {
        HttpStatusCode.Created
      }
    }

    client.get("/foo").let {
      assertSoftly {
        it.status shouldBe HttpStatusCode.Created
        it.bodyAsText() shouldBe ""
      }
    }
  }

  @Test
  fun `respond with empty when getOrRaise returns unit`() = testApplication {
    routing {
      getOrRaise("/foo", statusCode = HttpStatusCode.Created) {}
    }

    client.get("/foo").let {
      assertSoftly {
        it.status shouldBe HttpStatusCode.Created
        it.bodyAsText() shouldBe ""
      }
    }
  }

  @Test
  fun `respond with NoContent when getOrRaise returns unit and no explicit status code`() = testApplication {
    routing {
      getOrRaise("/foo") {}
    }

    client.get("/foo").let {
      assertSoftly {
        it.status shouldBe HttpStatusCode.NoContent
        it.bodyAsText() shouldBe ""
      }
    }
  }

  @Test
  fun `respond with raised statusCode when getOrRaise returns status code`() = testApplication {
    routing {
      getOrRaise<Unit>("/foo", statusCode = HttpStatusCode.Created) {
        ensureNotNull(emptyList<Unit>().firstOrNull()) { Response(HttpStatusCode.Conflict) }
      }
    }

    client.get("/foo").let {
      assertSoftly {
        it.status shouldBe HttpStatusCode.Conflict
        it.bodyAsText() shouldBe ""
      }
    }
  }

  @Test
  fun `receive and respond with raise`() = testApplication {
    routing {
      postOrRaise("/upper") {
        val body = call.receive<String>()
        body.uppercase()
      }
    }

    client.post("/upper") {
      setBody("hello")
    }.let {
      assertSoftly {
        it.status shouldBe OK
        it.bodyAsText() shouldBe "HELLO"
      }
    }
  }

  @Test
  fun `receive and respond typed with raise`() = testApplication {
    routing {
      postOrRaise<String, _>("/upper") { it.uppercase() }
    }

    client.post("/upper") {
      setBody("hello")
    }.let {
      assertSoftly {
        it.status shouldBe OK
        it.bodyAsText() shouldBe "HELLO"
      }
    }
  }

  @Test
  fun `custom domain error handling`() = testApplication {
    // example domain errors/service
    abstract class DomainError
    data class UserBanned(val userId: String) : DomainError()
    data class ServerError(val code: String, val message: String) : DomainError()

    data class User(val id: String)

    val userService = object {
      fun Raise<DomainError>.lookupUser(userId: String): User? = when (userId) {
        "bob" -> raise(UserBanned(userId))
        "alice" -> User(userId)
        else -> null
      }
    }

    // http error representation and handler
    data class ErrorPayload(val code: String, val message: String)

    fun handleError(domainError: DomainError): Response = when (domainError) {
      is UserBanned -> Unauthorized(ErrorPayload("Banned", domainError.userId))
      is ServerError -> InternalServerError(ErrorPayload("ServerError:${domainError.code}", domainError.message))
      else -> error("no local sealed class ;)")
    }

    // install basic `toString` content converter
    install(ContentNegotiation) {
      register(ContentType.Text.Plain, object : ContentConverter {
        override suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: ByteReadChannel): Nothing = TODO()
        override suspend fun serialize(contentType: ContentType, charset: Charset, typeInfo: TypeInfo, value: Any?) =
          TextContent(value.toString(), contentType)
      })
    }

    routing {
      getOrRaise("/users/{userId?}") {
        val userId = call.pathParameters["userId"] ?: raise(BadRequest("userId not specified"))
        withError(::handleError) {
          with(userService) {
            lookupUser(userId) ?: raise(HttpStatusCode.NotFound)
          }
        }
      }
    }

    client.get("/users/").let {
      assertSoftly {
        it.status shouldBe BadRequest
        it.bodyAsText() shouldBe "userId not specified"
      }
    }

    client.get("/users/alice").let {
      assertSoftly {
        it.status shouldBe OK
        it.bodyAsText() shouldBe "User(id=alice)"
      }
    }

    client.get("/users/bob").let {
      assertSoftly {
        it.status shouldBe Unauthorized
        it.bodyAsText() shouldBe "ErrorPayload(code=Banned, message=bob)"
      }
    }

    client.get("/users/carol").let {
      assertSoftly {
        it.status shouldBe HttpStatusCode.NotFound
        it.bodyAsText() shouldBe ""
      }
    }
  }
}
