package ar.edu.solicitudes;

public enum Estado {
    PENDIENTE, EN_PROCESO, RESUELTA, RECHAZADA;

    public static Estado desde(String texto) {
        return Enums.buscar(Estado.class, texto);
    }
}
