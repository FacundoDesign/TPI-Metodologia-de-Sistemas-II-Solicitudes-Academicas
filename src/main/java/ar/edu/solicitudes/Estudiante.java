package ar.edu.solicitudes;

// Datos del estudiante que inicia el tramite. Antes legajo, nombre y email viajaban siempre juntos
// como tres Strings sueltos (Data Clump).
public record Estudiante(String legajo, String nombre, String email) {
}
