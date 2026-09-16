sealed class Destino(val ruta: String) {
    object Lista : Destino("lista")
    object Crear : Destino("crear")
    object Detalle : Destino("detalle/{actividadId}") {
        fun crearRuta(id: Long) = "detalle/$id"
    }
    object Editar : Destino("editar/{actividadId}") {
        fun crearRuta(id: Long) = "editar/$id"
    }
}