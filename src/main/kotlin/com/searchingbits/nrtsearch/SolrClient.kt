package com.searchingbits.nrtsearch

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID

@Component
class SolrClient(private val restTemplate: RestTemplate, private val objectMapper: ObjectMapper) : SearchEngineClient {
    override fun addNewDoc(id: UUID) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        restTemplate.postForLocation(
                URI("http://localhost:8983/solr/nrt-search/update/json/docs"),
                HttpEntity("""
                    {
                        "id": "$id",
                        "created": "${LocalDateTime.now()}"
                    }
                """.trimIndent(), headers))
    }

    override fun findById(id: UUID): Boolean =
            foundBySolrUrl("http://localhost:8983/solr/nrt-search/select?q=id:$id")

    override fun getById(id: UUID): Boolean =
            foundBySolrUrl("http://localhost:8983/solr/nrt-search/get?ids=$id")

    private fun foundBySolrUrl(url: String): Boolean =
            objectMapper
                .readValue(restTemplate.getForObject(URI(url), String::class.java), SolrResponse::class.java)
                .response.numFound > 0
}

data class SolrResponse(val response: SolrResponseResponse)
data class SolrResponseResponse(val numFound: Int)