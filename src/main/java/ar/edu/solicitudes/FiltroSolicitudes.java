package ar.edu.solicitudes;

// Parameter Object: agrupa los criterios de busqueda (antes eran 5 parametros String intercambiables).
// Un criterio vacio significa "todos". La logica de coincidencia vive aqui, junto a los criterios (Move Method).
public record FiltroSolicitudes(String estado, String tipo, String prioridad, String responsable, String legajo) {

    // un criterio ausente (null) equivale a "todos": evita NullPointerException al combinar filtros
    public FiltroSolicitudes {
        estado = vacioSiNulo(estado);
        tipo = vacioSiNulo(tipo);
        prioridad = vacioSiNulo(prioridad);
        responsable = vacioSiNulo(responsable);
        legajo = vacioSiNulo(legajo);
    }

    public boolean acepta(Solicitud s) {
        String responsableActual = s.tieneResponsable() ? s.responsable().name() : "";
        return coincide(estado, s.estado().name())
                && coincide(tipo, s.tipo().name())
                && coincide(prioridad, s.prioridad().name())
                && coincide(responsable, responsableActual)
                && coincide(legajo, s.estudiante().legajo());
    }

    private static String vacioSiNulo(String criterio) {
        return criterio == null ? "" : criterio;
    }

    private static boolean coincide(String criterio, String valor) {
        return criterio.isEmpty() || valor.equals(criterio);
    }
}
