package com.antonio.samir.meteoritelandingsspots.service.repository.local

import android.location.Location
import androidx.lifecycle.LiveData
import com.antonio.samir.meteoritelandingsspots.service.business.model.Meteorite
import com.antonio.samir.meteoritelandingsspots.service.repository.local.database.MeteoriteDao
import com.antonio.samir.meteoritelandingsspots.service.repository.remote.NasaRemoteRepositoryInterface

class MeteoriteRepository(
        val meteoriteDao: MeteoriteDao,
        val nasaRemoteRepository: NasaRemoteRepositoryInterface
) : MeteoriteRepositoryInterface {

    override fun meteoriteOrdered(location: Location?, filter: String?): LiveData<List<Meteorite>> {

        return if (location == null) {
            if (filter.isNullOrEmpty()) {
                meteoriteDao.meteoriteOrdered()
            } else {
                meteoriteDao.meteoriteFiltered(filter)
            }
        } else {
            val lng = location.longitude
            val lat = location.latitude

            if (filter != null) {
                meteoriteDao.meteoriteOrderedByLocationFiltered(lat, lng, filter)
            } else {
                meteoriteDao.meteoriteOrderedByLocation(lat, lng)
            }
        }

    }

    override suspend fun getMeteoritesCount(): Int {
        return meteoriteDao.getMeteoritesCount()
    }

    override suspend fun insertAll(meteorites: List<Meteorite>) {
        meteoriteDao.insertAll(meteorites)
    }

    override suspend fun update(meteorite: Meteorite) {
        meteoriteDao.update(meteorite)
    }

    override fun getMeteoriteById(id: String): LiveData<Meteorite> {
        return meteoriteDao.getMeteoriteById(id)
    }

    override suspend fun meteoritesWithOutAddress(): List<Meteorite> {
        return meteoriteDao.meteoritesWithOutAddress()
    }

    override suspend fun getRemoteMeteorites(): List<Meteorite>? {
        return nasaRemoteRepository.getMeteorites()
    }

}