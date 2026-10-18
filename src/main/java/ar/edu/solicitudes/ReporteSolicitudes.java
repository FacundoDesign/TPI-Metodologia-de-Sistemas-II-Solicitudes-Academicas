package ar.edu.solicitudes;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

// Resultado del reporte como datos (no imprime nada): quien lo muestre decide el formato.
// Al recorrer Estado.values() y Prioridad.values(), un nuevo estado o prioridad aparece solo en el reporte (OCP).
public record ReporteSolicitudes(int total, Map<Estado, Integer> porEstado, Map<Prioridad, Integer> porPrioridad,
                                 int sinResponsable, double plazoPromedioDias) {

    public static ReporteSolicitudes generar(Collection<Solicitud> solicitudes) {
        Map<Estado, Integer> porEstado = new EnumMap<>(Estado.class);
        for (Estado e : Estado.values()) porEstado.put(e, 0);
        Map<Prioridad, Integer> porPrioridad = new EnumMap<>(Prioridad.class);
        for (Prioridad p : Prioridad.values()) porPrioridad.put(p, 0);
        int sinResponsable = 0;
        int sumaPlazos = 0;
        for (Solicitud s : solicitudes) {
            porEstado.merge(s.estado(), 1, Integer::sum);
            porPrioridad.merge(s.prioridad(), 1, Integer::sum);
            if (!s.tieneResponsable()) sinResponsable++;
            sumaPlazos += s.calcularPlazo();
        }
        return new ReporteSolicitudes(solicitudes.size(), porEstado, porPrioridad, sinResponsable,
                promedio(sumaPlazos, solicitudes.size()));
    }

    // promedio redondeado a un decimal; 0.0 si no hay solicitudes
    private static double promedio(int suma, int cantidad) {
        if (cantidad == 0) return 0.0;
        return Math.round((double) suma / cantidad * 10) / 10.0;
    }
}
