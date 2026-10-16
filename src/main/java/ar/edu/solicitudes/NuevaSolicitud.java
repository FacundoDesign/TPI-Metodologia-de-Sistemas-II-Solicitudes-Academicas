package ar.edu.solicitudes;

// Parameter Object: agrupa los datos de entrada del alta (antes eran 6 parametros sueltos).
// "tipo" llega como texto porque es lo que ingresa la persona; todavia no esta validado.
public record NuevaSolicitud(Estudiante estudiante, String tipo, String descripcion, boolean urgente) {
}
