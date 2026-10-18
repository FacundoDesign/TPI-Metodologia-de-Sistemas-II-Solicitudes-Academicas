package ar.edu.solicitudes;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class ReporteSolicitudesTest {

    @Test
    public void unReporteSinSolicitudesTieneTodoEnCeroYPlazoPromedioCero() {
        ReporteSolicitudes r = ReporteSolicitudes.generar(List.of());

        assertEquals(0, r.total());
        assertEquals(0, r.sinResponsable());
        assertEquals(0.0, r.plazoPromedioDias(), 0.0);
        for (Estado e : Estado.values()) assertEquals(Integer.valueOf(0), r.porEstado().get(e));
        for (Prioridad p : Prioridad.values()) assertEquals(Integer.valueOf(0), r.porPrioridad().get(p));
    }

    @Test
    public void cuentaPorEstadoPrioridadYSinResponsable() {
        List<Solicitud> datos = List.of(
                DatosDePrueba.solicitud(1, TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.PENDIENTE, null),
                DatosDePrueba.solicitud(2, TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.EN_PROCESO, Responsable.BEDELIA),
                DatosDePrueba.solicitud(3, TipoSolicitud.REVISION_NOTA, Prioridad.ALTA, Estado.RESUELTA, Responsable.BEDELIA));

        ReporteSolicitudes r = ReporteSolicitudes.generar(datos);

        assertEquals(3, r.total());
        assertEquals(Integer.valueOf(1), r.porEstado().get(Estado.PENDIENTE));
        assertEquals(Integer.valueOf(1), r.porEstado().get(Estado.EN_PROCESO));
        assertEquals(Integer.valueOf(1), r.porEstado().get(Estado.RESUELTA));
        assertEquals(Integer.valueOf(0), r.porEstado().get(Estado.RECHAZADA));
        assertEquals(Integer.valueOf(2), r.porPrioridad().get(Prioridad.BAJA));
        assertEquals(Integer.valueOf(1), r.porPrioridad().get(Prioridad.ALTA));
        assertEquals(1, r.sinResponsable());
    }

    @Test
    public void elPlazoPromedioSeRedondeaAUnDecimal() {
        // plazos: 10 + 10 + 8 = 28 -> 28 / 3 = 9.333... -> 9.3
        List<Solicitud> datos = List.of(
                DatosDePrueba.solicitud(1, TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.PENDIENTE, null),
                DatosDePrueba.solicitud(2, TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.PENDIENTE, null),
                DatosDePrueba.solicitud(3, TipoSolicitud.EQUIVALENCIA, Prioridad.ALTA, Estado.PENDIENTE, null));

        assertEquals(9.3, ReporteSolicitudes.generar(datos).plazoPromedioDias(), 0.0);
    }
}
