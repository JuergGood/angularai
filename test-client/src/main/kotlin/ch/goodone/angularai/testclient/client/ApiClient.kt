package ch.goodone.angularai.testclient.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ApiClient(private val baseUrl: String, private val auth: String) {

    companion object {
        private const val AUTH_HEADER = "Authorization"
        private const val ACCEPT_HEADER = "Accept"
        private const val CONTENT_TYPE_HEADER = "Content-Type"
        private const val JSON_TYPE = "application/json"
    }

    private val client: HttpClient = HttpClient.newBuilder().build()
    private val mapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun <T> get(path: String, responseType: Class<T>): T {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$path"))
            .header(AUTH_HEADER, "Basic $auth")
            .header(ACCEPT_HEADER, JSON_TYPE)
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        handleError(response)
        return mapper.readValue(response.body(), responseType)
    }

    fun <T> post(path: String, body: Any?, responseType: Class<T>): T {
        val bodyPublisher = if (body != null) {
            HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))
        } else {
            HttpRequest.BodyPublishers.noBody()
        }

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$path"))
            .header(AUTH_HEADER, "Basic $auth")
            .header(CONTENT_TYPE_HEADER, JSON_TYPE)
            .header(ACCEPT_HEADER, JSON_TYPE)
            .POST(bodyPublisher)
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        handleError(response)
        return mapper.readValue(response.body(), responseType)
    }

    fun put(path: String, body: Any?): Boolean {
        val bodyPublisher = if (body != null) {
            HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))
        } else {
            HttpRequest.BodyPublishers.noBody()
        }

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$path"))
            .header(AUTH_HEADER, "Basic $auth")
            .header(CONTENT_TYPE_HEADER, JSON_TYPE)
            .PUT(bodyPublisher)
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        handleError(response)
        return response.statusCode() in 200..299
    }

    fun delete(path: String): Boolean {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$path"))
            .header(AUTH_HEADER, "Basic $auth")
            .DELETE()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        handleError(response)
        return response.statusCode() in 200..299
    }

    private fun handleError(response: HttpResponse<String>) {
        if (response.statusCode() !in 200..299) {
            throw RuntimeException("API error: ${response.statusCode()} - ${response.body()}")
        }
    }
}
