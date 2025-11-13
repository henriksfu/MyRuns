package com.example.henrik_sachdeva_myruns4.database

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import java.util.Date

class Converters {

    // Date ↔ Timestamp
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    // List<LatLng> ↔ String
    @TypeConverter
    fun fromLatLngList(list: List<LatLng>?): String {
        return list?.joinToString(";") { "${it.latitude},${it.longitude}" }.orEmpty()
    }

    @TypeConverter
    fun toLatLngList(data: String?): MutableList<LatLng> {
        if (data.isNullOrEmpty()) return mutableListOf()

        return data.split(";").map { entry ->
            val (lat, lon) = entry.split(",")
            LatLng(lat.toDouble(), lon.toDouble())
        }.toMutableList()
    }
}
