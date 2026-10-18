package ar.edu.solicitudes;

// Parameter Object: agrupa los datos de entrada del alta (antes eran 6 parametros sueltos).
// Ya llega validado y tipado: el estudiante se valida a si mismo y el tipo es un enum.
public record NuevaSolicitud(Estudiante estudiante, TipoSolicitud tipo, String descripcion, boolean urgente) {

    public NuevaSolicitud {
        descripcion = descripcion == null ? "" : descripcion;
    }
}
