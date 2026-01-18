package ch.goodone.angularai.testclient.client

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

class ApiClientTest {

    @Test
    fun `test handleError should throw exception on non-2xx`() {
        val client = ApiClient("http://localhost:8080", "auth")
        @Suppress("UNCHECKED_CAST")
        val response = mock(HttpResponse::class.java) as HttpResponse<String>
        
        `when`(response.statusCode()).thenReturn(404)
        `when`(response.body()).thenReturn("Not Found")

        val method = ApiClient::class.java.getDeclaredMethod("handleError", HttpResponse::class.java)
        method.isAccessible = true

        val exception = assertThrows<RuntimeException> {
            try {
                method.invoke(client, response)
            } catch (e: java.lang.reflect.InvocationTargetException) {
                throw e.targetException
            }
        }
        
        assertTrue(exception.message!!.contains("404"))
        assertTrue(exception.message!!.contains("Not Found"))
    }

    @Test
    fun `test handleError should not throw exception on 2xx`() {
        val client = ApiClient("http://localhost:8080", "auth")
        @Suppress("UNCHECKED_CAST")
        val response = mock(HttpResponse::class.java) as HttpResponse<String>
        
        `when`(response.statusCode()).thenReturn(200)

        val method = ApiClient::class.java.getDeclaredMethod("handleError", HttpResponse::class.java)
        method.isAccessible = true
        
        method.invoke(client, response) // should not throw
    }

    @Test
    fun `test get should return object`() {
        val mockHttpClient = mock<HttpClient>()
        val mockResponse = mock<HttpResponse<String>>()
        `when`(mockResponse.statusCode()).thenReturn(200)
        `when`(mockResponse.body()).thenReturn("{\"login\":\"test\"}")
        `when`(mockHttpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>())).thenReturn(mockResponse)

        val client = ApiClient("http://localhost:8080", "auth")
        val field = ApiClient::class.java.getDeclaredField("client")
        field.isAccessible = true
        field.set(client, mockHttpClient)

        val result = client.get("/api/test", Map::class.java)
        assertEquals("test", result["login"])
    }

    @Test
    fun `test post should return object`() {
        val mockHttpClient = mock<HttpClient>()
        val mockResponse = mock<HttpResponse<String>>()
        `when`(mockResponse.statusCode()).thenReturn(201)
        `when`(mockResponse.body()).thenReturn("{\"id\":1}")
        `when`(mockHttpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>())).thenReturn(mockResponse)

        val client = ApiClient("http://localhost:8080", "auth")
        val field = ApiClient::class.java.getDeclaredField("client")
        field.isAccessible = true
        field.set(client, mockHttpClient)

        val result = client.post("/api/test", mapOf("name" to "test"), Map::class.java)
        assertEquals(1, result["id"])
    }

    @Test
    fun `test put should return true`() {
        val mockHttpClient = mock<HttpClient>()
        val mockResponse = mock<HttpResponse<String>>()
        `when`(mockResponse.statusCode()).thenReturn(204)
        `when`(mockResponse.body()).thenReturn("")
        `when`(mockHttpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>())).thenReturn(mockResponse)

        val client = ApiClient("http://localhost:8080", "auth")
        val field = ApiClient::class.java.getDeclaredField("client")
        field.isAccessible = true
        field.set(client, mockHttpClient)

        val result = client.put("/api/test", mapOf("name" to "test"))
        assertTrue(result)
    }

    @Test
    fun `test delete should return true`() {
        val mockHttpClient = mock<HttpClient>()
        val mockResponse = mock<HttpResponse<String>>()
        `when`(mockResponse.statusCode()).thenReturn(204)
        `when`(mockResponse.body()).thenReturn("")
        `when`(mockHttpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>())).thenReturn(mockResponse)

        val client = ApiClient("http://localhost:8080", "auth")
        val field = ApiClient::class.java.getDeclaredField("client")
        field.isAccessible = true
        field.set(client, mockHttpClient)

        val result = client.delete("/api/test")
        assertTrue(result)
    }
}
