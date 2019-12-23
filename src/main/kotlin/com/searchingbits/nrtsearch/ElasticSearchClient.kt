package com.searchingbits.nrtsearch

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID

@Component
class ElasticSearchClient(private val restTemplate: RestTemplate, private val objectMapper: ObjectMapper) : SearchEngineClient {
    override fun addNewDoc(id: UUID) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        restTemplate.put(
                URI("http://localhost:9200/nrt-search/doc/$id"),
                HttpEntity("""
                    {
                        "id": "$id",
                        "created": "${LocalDateTime.now()}"
                    }
                """.trimIndent(), headers))
    }

    override fun findById(id: UUID): Boolean {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val response = restTemplate.exchange(
                URI("http://localhost:9200/nrt-search/doc/_search"),
                HttpMethod.POST,
                HttpEntity("""
                    {
                        "query" : {
                            "match" : {
                                "_id" : "$id"
                            }
                        }
                    }
                """.trimIndent(), headers),
                ElasticSearchResponse::class.java).body

        return response?.hits?.total?.value?.let { it > 0 } ?: false
    }

    override fun getById(id: UUID): Boolean =
            restTemplate
                .getForObject(URI("http://localhost:9200/nrt-search/doc/$id"), ElasticSearchGetResponse::class.java)
                ?.found ?: false
}

private data class ElasticSearchResponse(val hits: ResponseHits)
private data class ResponseHits(val total: ResponseHitsTotal)
private data class ResponseHitsTotal(val value: Int)

private data class ElasticSearchGetResponse(val found: Boolean)