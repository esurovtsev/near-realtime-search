package com.searchingbits.nrtsearch

import com.google.common.base.Stopwatch
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import java.util.UUID
import kotlin.math.max

data class Document(val id: UUID, val name: String)

@ShellComponent
class Commands(
        private val solrClient: SolrClient,
        private val elasticSearchClient: ElasticSearchClient
) {
    private val numOfMeasurements = 10

    @ShellMethod("Test NRT feature for ElasticSearch")
    fun elasticSearchTest() =
            testSearchEngine(elasticSearchClient)

    @ShellMethod("Test NRT feature for Solr")
    fun solrTest() =
            testSearchEngine(solrClient)

    private fun testSearchEngine(searchEngineClient: SearchEngineClient): List<String> {
        val nrtTimeInMillis = testOperation(searchEngineClient) { searchEngineClient.findById(it) }
        val realTimeInMillis = testOperation(searchEngineClient) { searchEngineClient.getById(it) }
        return listOf(
                "NearRealTime Search: $nrtTimeInMillis",
                "RealTime Get: $realTimeInMillis"
        )
    }

    private fun testOperation(searchEngineClient: SearchEngineClient, findOp: (UUID) -> Boolean): String {
        var measurements: Long = 0
        var measurementAttempt = 0;

        for (i in 1..numOfMeasurements) {
            val id = UUID.randomUUID()
            searchEngineClient.addNewDoc(id)
            val stopwatch = Stopwatch.createStarted()
            var found = false
            var attempt = 0;
            while (!found) {
                found = findOp(id)
                attempt++
            }
            stopwatch.stop()
            measurements += stopwatch.elapsed().toMillis()
            measurementAttempt = max(measurementAttempt, attempt)
        }

        return "${measurements / numOfMeasurements} ms. (calls: $measurementAttempt)"
    }
}