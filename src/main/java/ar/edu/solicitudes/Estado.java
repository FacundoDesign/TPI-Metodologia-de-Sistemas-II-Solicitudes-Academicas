package ar.edu.solicitudes;

public enum Estado {
    PENDIENTE, EN_PROCESO, RESUELTA, RECHAZADA;

    public static Estado desde(String texto) {
        return Enums.buscar(Estado.class, texto);
    }

    // un estado final no admite mas cambios ni asignaciones
    public boolean esFinal() {
        return this == RESUELTA || this == RECHAZADA;
    }

    // maquina de estados del tramite: PENDIENTE -> EN_PROCESO -> RESUELTA, y rechazo desde cualquier estado abierto
    public boolean puedePasarA(Estado nuevo) {
        return switch (this) {
            case PENDIENTE -> nuevo == EN_PROCESO || nuevo == RECHAZADA;
            case EN_PROCESO -> nuevo == RESUELTA || nuevo == RECHAZADA;
            case RESUELTA, RECHAZADA -> false;
        };
    }
}
