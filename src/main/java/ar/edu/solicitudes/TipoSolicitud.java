package ar.edu.solicitudes;

public enum TipoSolicitud {
    CERTIFICADO, EQUIVALENCIA, REVISION_NOTA, BAJA_MATERIA;

    public static TipoSolicitud desde(String texto) {
        return Enums.buscar(TipoSolicitud.class, texto);
    }
}
