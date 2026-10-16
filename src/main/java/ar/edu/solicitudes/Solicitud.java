package ar.edu.solicitudes;

public class Solicitud {
    public int id;
    public String legajo;
    public String nombre;
    public String email;
    public TipoSolicitud tipo;
    public String descripcion;
    public Prioridad prioridad;
    public Estado estado;
    public Responsable responsable;
    public boolean urgente;

    // plazo de respuesta en dias habiles: depende de la prioridad y del tipo de tramite
    public int calcularPlazo() {
        return prioridad.plazoBaseDias() + tipo.plazoExtraDias();
    }

    public boolean estaCerrada() {
        return estado.esFinal();
    }

    public boolean tieneResponsable() {
        return responsable != null;
    }
}
