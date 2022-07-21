package com.example.mapbox

data class GeoJSON (
    var type: String,
    val features: List<Feature>
)

data class Feature (
    val type: FeatureType,
    val properties: Properties,
    val geometry: Geometry
)

data class Geometry (
    val type: GeometryType,
    val coordinates: List<Double>
)

enum class GeometryType {
    Point
}

data class Properties (
    val scalerank: Long,
    val natscale: Long,
    val labelrank: Long,
    val name: String,
    val namepar: String? = null,
    val namealt: String? = null,
    val diffascii: Long,
    val nameascii: String,
    val adm0Cap: Long,
    val capalt: Long,
    val capin: String? = null,
    val worldcity: Long,
    val megacity: Long,
    val sov0Name: String,
    val sovA3: String,
    val adm0Name: String,
    val adm0A3: String,
    val adm1Name: String? = null,
    val isoA2: String,
    val note: String? = null,
    val latitude: Double,
    val longitude: Double,
    val changed: Long,
    val namediff: Long,
    val diffnote: String? = null,
    val popMax: Long,
    val popMin: Long,
    val popOther: Long,
    val rankMax: Long,
    val rankMin: Long,
    val geonameid: Long,
    val meganame: String? = null,
    val lsName: String? = null,
    val lsMatch: Long,
    val checkme: Long,
    val featureclass: Featureclass
)

enum class Featureclass {
    Admin0Capital,
    Admin0CapitalAlt,
    Admin0RegionCapital,
    Admin1Capital,
    Admin1RegionCapital,
    HistoricPlace,
    PopulatedPlace,
    ScientificStation
}

enum class FeatureType {
    Feature
}
