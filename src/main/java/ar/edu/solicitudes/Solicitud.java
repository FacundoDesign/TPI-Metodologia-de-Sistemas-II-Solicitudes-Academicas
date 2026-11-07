package ar.edu.solicitudes;

// Entidad de dominio. Encapsula sus datos y hace cumplir sus propias reglas (maquina de estados, asignacion),
// de modo que nadie pueda dejarla en un estado inconsistente desde afuera.
public class Solicitud {

    private final int id;
    private final Estudiante estudiante;
    private final TipoSolicitud tipo;
    private final String descripcion;
    private final Prioridad prioridad;
    private final boolean urgente;
    private Estado estado;
    private Responsable responsable;

    private Solicitud(int id, NuevaSolicitud datos, Prioridad prioridad, Seguimiento seguimiento) {
        this.id = id;
        this.estudiante = datos.estudiante();
        this.tipo = datos.tipo();
        this.descripcion = datos.descripcion();
        this.urgente = datos.urgente();
        this.prioridad = prioridad;
        this.estado = seguimiento.estado();
        this.responsable = seguimiento.responsable();
    }

    // Alta de una solicitud nueva: nace PENDIENTE, sin responsable y con la prioridad que corresponde al tipo.
    public static Solicitud nueva(int id, NuevaSolicitud datos) {
        return new Solicitud(id, datos, datos.tipo().prioridadInicial(datos.urgente()),
                new Seguimiento(Estado.PENDIENTE, null));
    }

    // Reconstruccion de una solicitud ya existente (la usa el repositorio al leer los datos guardados).
    // Reutiliza NuevaSolicitud y Seguimiento como Parameter Objects: 4 parametros en lugar de 8.
    public static Solicitud reconstruir(int id, NuevaSolicitud datos, Prioridad prioridad, Seguimiento seguimiento) {
        return new Solicitud(id, datos, prioridad, seguimiento);
    }

    public int id() {
        return id;
    }

    public Estudiante estudiante() {
        return estudiante;
    }

    public TipoSolicitud tipo() {
        return tipo;
    }

    public String descripcion() {
        return descripcion;
    }

    public Prioridad prioridad() {
        return prioridad;
    }

    public boolean urgente() {
        return urgente;
    }

    public Estado estado() {
        return estado;
    }

    // puede ser null mientras no se haya asignado; usar tieneResponsable() para consultarlo
    public Responsable responsable() {
        return responsable;
    }

    // plazo de respuesta en dias habiles: depende de la prioridad y del tipo de tramite
    public int calcularPlazo() {
        return prioridad.plazoBaseDias() + tipo.plazoExtraDias();
    }

    public boolean estaCerrada() {
        return estado.esFinal();
    }

    // Foto del estado mutable (estado + responsable): permite al servicio deshacer un cambio si no se pudo persistir.
    Seguimiento seguimiento() {
        return new Seguimiento(estado, responsable);
    }

    void restaurar(Seguimiento anterior) {
        this.estado = anterior.estado();
        this.responsable = anterior.responsable();
    }

    public boolean tieneResponsable() {
        return responsable != null;
    }

    public void pasarA(Estado nuevoEstado) {
        if (!estado.puedePasarA(nuevoEstado)) {
            throw new SolicitudException("transicion invalida " + estado + " -> " + nuevoEstado);
        }
        if (nuevoEstado == Estado.EN_PROCESO && !tieneResponsable()) {
            throw new SolicitudException("debe asignar responsable antes de pasar a EN_PROCESO");
        }
        this.estado = nuevoEstado;
    }

    public void asignarA(Responsable nuevoResponsable) {
        if (estaCerrada()) throw new SolicitudException("la solicitud esta cerrada");
        this.responsable = nuevoResponsable;
    }
}
