package ch.goodone.angularai.testclient.command

import ch.goodone.angularai.testclient.client.ApiClient

interface Command {
    fun execute(client: ApiClient, args: List<String>)
}
