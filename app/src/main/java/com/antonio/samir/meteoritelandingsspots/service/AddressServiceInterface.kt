package com.antonio.samir.meteoritelandingsspots.service

import com.antonio.samir.meteoritelandingsspots.common.ResultOf
import com.antonio.samir.meteoritelandingsspots.data.repository.model.Meteorite
import kotlinx.coroutines.flow.Flow

interface AddressServiceInterface {

    fun recoveryAddress(): Flow<ResultOf<Float>>

    suspend fun recoverAddress(meteorite: Meteorite)

}