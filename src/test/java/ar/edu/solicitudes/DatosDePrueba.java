package ar.edu.solicitudes;

/** Constructores de datos sinteticos para las pruebas (no hay datos personales reales). */
final class DatosDePrueba {

    private DatosDePrueba() {
    }

    static Estudiante ana() {
        return new Estudiante("12345", "Ana Perez", "ana@mail.com");
    }

    static NuevaSolicitud pedido(TipoSolicitud tipo, boolean urgente) {
        return new NuevaSolicitud(ana(), tipo, "descripcion de prueba", urgente);
    }

    static Solicitud solicitud(int id, TipoSolicitud tipo, Prioridad prioridad, Estado estado, Responsable responsable) {
        return Solicitud.reconstruir(id, ana(), tipo, "descripcion de prueba", prioridad, estado, responsable, false);
    }
}
