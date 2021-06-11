package com.antonio.samir.meteoritelandingsspots.data.local

import androidx.paging.DataSource
import androidx.paging.PagingSource
import com.antonio.samir.meteoritelandingsspots.data.repository.model.Meteorite
import kotlinx.coroutines.flow.Flow

interface MeteoriteLocalRepository {

    fun meteoriteOrdered(
            filter: String?,
            latitude: Double?,
            longitude: Double?,
            limit: Long,
    ): PagingSource<Int, Meteorite>

    fun meteoritesWithOutAddress(): Flow<List<Meteorite>>

    fun getMeteoriteById(id: String): Flow<Meteorite>

    suspend fun update(meteorite: Meteorite)

    suspend fun getMeteoritesCount(): Int

    suspend fun getMeteoritesWithoutAddressCount(): Int

    suspend fun insertAll(meteorites: List<Meteorite>)

    suspend fun updateAll(meteorites: List<Meteorite>)

}
