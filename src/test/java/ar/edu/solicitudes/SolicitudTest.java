package ar.edu.solicitudes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SolicitudTest {

    private Solicitud solicitud(TipoSolicitud tipo, Prioridad prioridad, Estado estado, Responsable responsable) {
        Solicitud s = new Solicitud();
        s.tipo = tipo;
        s.prioridad = prioridad;
        s.estado = estado;
        s.responsable = responsable;
        return s;
    }

    @Test
    public void plazoDeUnCertificadoDePrioridadBajaEsDiezDias() {
        assertEquals(10, solicitud(TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.PENDIENTE, null).calcularPlazo());
    }

    @Test
    public void plazoDeUnaEquivalenciaSumaCincoDias() {
        assertEquals(8, solicitud(TipoSolicitud.EQUIVALENCIA, Prioridad.ALTA, Estado.PENDIENTE, null).calcularPlazo());
        assertEquals(10, solicitud(TipoSolicitud.EQUIVALENCIA, Prioridad.MEDIA, Estado.PENDIENTE, null).calcularPlazo());
        assertEquals(15, solicitud(TipoSolicitud.EQUIVALENCIA, Prioridad.BAJA, Estado.PENDIENTE, null).calcularPlazo());
    }

    @Test
    public void plazoDeUnaRevisionDeNotaAltaEsTresDias() {
        assertEquals(3, solicitud(TipoSolicitud.REVISION_NOTA, Prioridad.ALTA, Estado.PENDIENTE, null).calcularPlazo());
    }

    @Test
    public void estaCerradaSoloEnEstadosFinales() {
        assertFalse(solicitud(TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.PENDIENTE, null).estaCerrada());
        assertFalse(solicitud(TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.EN_PROCESO, null).estaCerrada());
        assertTrue(solicitud(TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.RESUELTA, null).estaCerrada());
        assertTrue(solicitud(TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.RECHAZADA, null).estaCerrada());
    }

    @Test
    public void tieneResponsableSegunSeHayaAsignado() {
        assertFalse(solicitud(TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.PENDIENTE, null).tieneResponsable());
        assertTrue(solicitud(TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.PENDIENTE, Responsable.BEDELIA).tieneResponsable());
    }
}
