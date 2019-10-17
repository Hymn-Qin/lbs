package com.hymnal.lbs

data class Gps(val latitude: Double, val longitude: Double) {

    override fun toString(): String {
        return "$latitude,$longitude"
    }
}
