package sample.internal

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.readValue
import jstack.di.DiContext
import jstack.di.retrieve
import jstack.log.debug
import jstack.log.error
import jstack.log.logger
import sample.common.Client
import sample.common.ObjectMapper
import sample.models.Pet
import sample.models.Status
import java.io.InputStream
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpRequest.newBuilder
import java.net.http.HttpResponse.BodyHandlers

private const val URL = "https://petstore.swagger.io/v2"

fun DiContext.petFindByStatus(statuses: List<Status>): List<Pet> {
    val queryParams = statuses.joinToString("&") { "status=$it" }
    return request<List<Pet>>(newBuilder()
            .GET()
            .uri(URI.create("$URL/pet/findByStatus?$queryParams"))
            .build()).also {
        val log by logger()
        log.debug { put("pets", it.size) }
    }
}

fun DiContext.storeInventory() = request<Map<String, Int>>(newBuilder()
    .GET()
    .uri(URI.create("$URL/store/inventory"))
    .build())

private inline fun <reified R> DiContext.request(req: HttpRequest): R {
    val client = retrieve(Client)
    val res = client.send(req, BodyHandlers.ofInputStream())
    return if (res.statusCode() == 200) {
         json(res.body())
    } else {
        val log by logger()
        log.error {
            put("statusCode", res.statusCode())
            put("headers", res.headers())
        }
        error("Status ${res.statusCode()} from petstore api")    }
}

private inline fun <reified R> DiContext.json(inputStream: InputStream): R =
    retrieve(ObjectMapper).readValue(inputStream)
