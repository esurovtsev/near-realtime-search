package com.searchingbits.nrtsearch

import com.google.common.base.Stopwatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import java.util.UUID
import kotlin.math.max

@ShellComponent
class Commands(
        private val solrClient: SolrClient,
        private val elasticSearchClient: ElasticSearchClient
) {
    private val numOfMeasurements = 10

    @ShellMethod("Test Near RealTime feature for Solr & ElasticSearch")
    fun testNearRealTime() =
            listOf("Solr:").plus(testSearchEngine(solrClient)).plusElement("")
                .plusElement("Elastic Search:").plus(testSearchEngine(elasticSearchClient))

    private fun testSearchEngine(searchEngineClient: SearchEngineClient): List<String> {
        val nrtTimeInMillis = testOperation(searchEngineClient) { searchEngineClient.findById(it) }
        val realTimeInMillis = testOperation(searchEngineClient) { searchEngineClient.getById(it) }
        return listOf(
                "Near RealTime Search: $nrtTimeInMillis",
                "RealTime Get: $realTimeInMillis"
        )
    }

    private fun testOperation(searchEngineClient: SearchEngineClient, findOp: (UUID) -> Boolean): String {
        var measurements: Long = 0
        var measurementAttempt = 0;

        for (i in 1..numOfMeasurements) {
            val id = UUID.randomUUID()
            log.debug("Adding new document using {}", searchEngineClient.javaClass.simpleName)
            searchEngineClient.addNewDoc(id)
            val stopwatch = Stopwatch.createStarted()
            var found = false
            var attempt = 0;
            while (!found) {
                log.debug("Searching document using {}", searchEngineClient.javaClass.simpleName)
                found = findOp(id)
                attempt++
            }
            stopwatch.stop()
            measurements += stopwatch.elapsed().toMillis()
            measurementAttempt = max(measurementAttempt, attempt)
        }

        return "${measurements / numOfMeasurements} ms. (calls: $measurementAttempt)"
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(Commands::class.java)
    }
}