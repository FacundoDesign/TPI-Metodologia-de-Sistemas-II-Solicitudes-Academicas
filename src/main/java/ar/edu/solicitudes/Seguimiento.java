package ar.edu.solicitudes;

// Parameter Object: el estado del tramite y su responsable cambian siempre juntos (Data Clump).
// Agruparlos baja Solicitud.reconstruir de 5 a 4 parametros y evita intercambiar por error dos valores del mismo tipo.
// El responsable es null mientras no se haya asignado.
public record Seguimiento(Estado estado, Responsable responsable) {
}
