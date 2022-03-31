package com.antonio.samir.meteoritelandingsspots.features.list.mapper

import android.content.Context
import android.location.Location
import com.antonio.samir.meteoritelandingsspots.common.mapper.MapperBase
import com.antonio.samir.meteoritelandingsspots.data.repository.model.Meteorite
import com.antonio.samir.meteoritelandingsspots.features.getDistanceFrom
import com.antonio.samir.meteoritelandingsspots.features.getLocationText
import com.antonio.samir.meteoritelandingsspots.features.list.MeteoriteItemView
import com.antonio.samir.meteoritelandingsspots.features.yearString

class MeteoriteViewMapper(val context: Context) :
    MapperBase<MeteoriteViewMapper.Input, MeteoriteItemView>() {

    override suspend fun action(input: Input): MeteoriteItemView {

        val meteorite = input.meteorite

        val address = meteorite.getLocationText(
            noAddress = ""
        )

        return MeteoriteItemView(
            id = "${meteorite.id}",
            name = meteorite.name ?: "---",
            yearString = meteorite.yearString ?: "---",
            address = address,
            hasAddress = address.isNotBlank(),
            distance = meteorite.getDistanceFrom(
                input.location
            ) ?: ""
        )
    }

    data class Input(val meteorite: Meteorite, val location: Location?)

}