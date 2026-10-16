package ar.edu.solicitudes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TipoSolicitudTest {

    @Test
    public void prioridadInicialSinUrgencia() {
        assertEquals(Prioridad.BAJA, TipoSolicitud.CERTIFICADO.prioridadInicial(false));
        assertEquals(Prioridad.MEDIA, TipoSolicitud.EQUIVALENCIA.prioridadInicial(false));
        assertEquals(Prioridad.ALTA, TipoSolicitud.REVISION_NOTA.prioridadInicial(false));
        assertEquals(Prioridad.MEDIA, TipoSolicitud.BAJA_MATERIA.prioridadInicial(false));
    }

    @Test
    public void laUrgenciaSubeUnNivel() {
        assertEquals(Prioridad.MEDIA, TipoSolicitud.CERTIFICADO.prioridadInicial(true));
        assertEquals(Prioridad.ALTA, TipoSolicitud.EQUIVALENCIA.prioridadInicial(true));
        assertEquals(Prioridad.ALTA, TipoSolicitud.REVISION_NOTA.prioridadInicial(true));
        assertEquals(Prioridad.ALTA, TipoSolicitud.BAJA_MATERIA.prioridadInicial(true));
    }

    @Test
    public void soloLaEquivalenciaAgregaDiasDePlazo() {
        assertEquals(0, TipoSolicitud.CERTIFICADO.plazoExtraDias());
        assertEquals(5, TipoSolicitud.EQUIVALENCIA.plazoExtraDias());
        assertEquals(0, TipoSolicitud.REVISION_NOTA.plazoExtraDias());
        assertEquals(0, TipoSolicitud.BAJA_MATERIA.plazoExtraDias());
    }
}
