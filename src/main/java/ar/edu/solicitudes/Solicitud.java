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
}
