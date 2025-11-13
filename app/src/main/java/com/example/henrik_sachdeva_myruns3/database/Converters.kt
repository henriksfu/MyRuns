package com.example.henrik_sachdeva_myruns3.database

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import java.util.Date

class Converters {

    // ----------------------------
    // DATE ↔ TIMESTAMP
    // ----------------------------
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // ----------------------------
    // GPS LIST ↔ STRING
    // ----------------------------
    @TypeConverter
    fun fromLatLngList(list: List<LatLng>?): String {
        if (list == null) return ""
        return list.joinToString(";") { "${it.latitude},${it.longitude}" }
    }

    @TypeConverter
    fun toLatLngList(data: String?): MutableList<LatLng> {
        if (data.isNullOrEmpty()) return mutableListOf()

        return data.split(";").map { pair ->
            val parts = pair.split(",")
            val lat = parts[0].toDouble()
            val lon = parts[1].toDouble()
            LatLng(lat, lon)
        }.toMutableList()
    }
}
