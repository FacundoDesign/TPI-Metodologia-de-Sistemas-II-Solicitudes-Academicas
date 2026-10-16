package ar.edu.solicitudes;

public enum Prioridad {
    ALTA(3), MEDIA(5), BAJA(10);

    private final int plazoBaseDias;

    Prioridad(int plazoBaseDias) {
        this.plazoBaseDias = plazoBaseDias;
    }

    public static Prioridad desde(String texto) {
        return Enums.buscar(Prioridad.class, texto);
    }

    public int plazoBaseDias() {
        return plazoBaseDias;
    }

    // una solicitud urgente sube un nivel; la prioridad ALTA no puede subir mas
    public Prioridad subir() {
        return switch (this) {
            case BAJA -> MEDIA;
            case MEDIA, ALTA -> ALTA;
        };
    }
}
