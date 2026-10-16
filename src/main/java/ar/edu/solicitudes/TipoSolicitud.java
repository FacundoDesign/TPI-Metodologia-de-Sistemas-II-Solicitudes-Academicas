package ar.edu.solicitudes;

public enum TipoSolicitud {
    CERTIFICADO(Prioridad.BAJA, 0),
    EQUIVALENCIA(Prioridad.MEDIA, 5),
    REVISION_NOTA(Prioridad.ALTA, 0),
    BAJA_MATERIA(Prioridad.MEDIA, 0);

    private final Prioridad prioridadBase;
    private final int plazoExtraDias;

    TipoSolicitud(Prioridad prioridadBase, int plazoExtraDias) {
        this.prioridadBase = prioridadBase;
        this.plazoExtraDias = plazoExtraDias;
    }

    public static TipoSolicitud desde(String texto) {
        return Enums.buscar(TipoSolicitud.class, texto);
    }

    public Prioridad prioridadInicial(boolean urgente) {
        return urgente ? prioridadBase.subir() : prioridadBase;
    }

    public int plazoExtraDias() {
        return plazoExtraDias;
    }
}
