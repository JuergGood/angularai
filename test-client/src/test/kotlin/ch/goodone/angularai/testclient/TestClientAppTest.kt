package ch.goodone.angularai.testclient

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TestClientAppTest {

    @Test
    fun `test parseArgs with options and commands`() {
        val args = arrayOf("--env", "local", "load", "all", "--count", "10", "--flag")
        
        val method = ::main.javaClass.classLoader.loadClass("ch.goodone.angularai.testclient.TestClientAppKt")
            .getDeclaredMethod("parseArgs", Array<String>::class.java)
        method.isAccessible = true
        
        @Suppress("UNCHECKED_CAST")
        val result = method.invoke(null, args) as Pair<Map<String, String>, List<String>>
        val options = result.first
        val commands = result.second
        
        assertEquals("local", options["--env"])
        assertEquals("10", options["--count"])
        assertEquals("true", options["--flag"])
        assertEquals(listOf("load", "all"), commands)
    }

    @Test
    fun `test getBaseUrl`() {
        val method = ::main.javaClass.classLoader.loadClass("ch.goodone.angularai.testclient.TestClientAppKt")
            .getDeclaredMethod("getBaseUrl", String::class.java)
        method.isAccessible = true
        
        assertEquals("http://localhost:8080", method.invoke(null, "local"))
        assertEquals("https://www.goodone.ch/api", method.invoke(null, "aws"))
        assertEquals("http://custom:8080", method.invoke(null, "http://custom:8080"))
    }

    @Test
    fun `test printUsage`() {
        printUsage() // just for coverage
    }
}
