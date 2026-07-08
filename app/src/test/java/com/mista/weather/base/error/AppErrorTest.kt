package com.mista.weather.base.error

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AppErrorTest {

    @Test
    fun `an existing AppError passes through unchanged`() {
        val original = AppError.Network()
        assertSame(original, original.toAppError())
    }

    @Test
    fun `IOException maps to a Network error`() {
        val ioException = IOException("boom")
        val result = ioException.toAppError()
        assertTrue(result is AppError.Network)
        assertSame(ioException, result.cause)
    }

    @Test
    fun `HttpException maps to an Http error carrying the status code and body`() {
        val body = """{"message":"not found"}"""
        val httpException = HttpException(
            Response.error<Any>(404, body.toResponseBody("application/json".toMediaTypeOrNull())),
        )

        val result = httpException.toAppError()

        assertTrue(result is AppError.Http)
        result as AppError.Http
        assertEquals(404, result.code)
        assertEquals(body, result.errorBody)
    }

    @Test
    fun `any other throwable maps to Unexpected`() {
        val exception = IllegalStateException("weird")
        val result = exception.toAppError()
        assertTrue(result is AppError.Unexpected)
        assertSame(exception, result.cause)
    }

    @Test
    fun `ApiErrorBody prefers message over the other fields`() {
        val body = ApiErrorBody(message = "explicit message", error = "error field", detail = "detail field")
        assertEquals("explicit message", body.displayMessage)
    }

    @Test
    fun `ApiErrorBody falls back through error, detail, then first of errors`() {
        assertEquals("error field", ApiErrorBody(error = "error field", detail = "detail field").displayMessage)
        assertEquals("detail field", ApiErrorBody(detail = "detail field").displayMessage)
        assertEquals("first", ApiErrorBody(errors = listOf("first", "second")).displayMessage)
        assertNull(ApiErrorBody().displayMessage)
    }

    @Test
    fun `Http toApiErrorBody parses the raw error body JSON`() {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val httpError = AppError.Http(code = 400, errorBody = """{"message":"bad request"}""")

        val parsed = httpError.toApiErrorBody(moshi)

        assertEquals("bad request", parsed?.message)
    }

    @Test
    fun `Http toApiErrorBody returns null for malformed json`() {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val httpError = AppError.Http(code = 400, errorBody = "not json")

        assertNull(httpError.toApiErrorBody(moshi))
    }
}
