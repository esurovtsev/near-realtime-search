package com.searchingbits.nrtsearch

import java.util.UUID

interface SearchEngineClient {
    fun addNewDoc(id: UUID)
    fun findById(id: UUID): Boolean
    fun getById(id: UUID): Boolean
}