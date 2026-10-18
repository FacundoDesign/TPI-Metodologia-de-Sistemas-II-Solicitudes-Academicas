package ar.edu.solicitudes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Reglas que la entidad Solicitud hace cumplir por si misma (maquina de estados y asignacion). */
public class SolicitudReglasTest {

    private Solicitud nueva(TipoSolicitud tipo, boolean urgente) {
        return Solicitud.nueva(1, DatosDePrueba.pedido(tipo, urgente));
    }

    @Test
    public void nuevaNacePendienteSinResponsable() {
        Solicitud s = nueva(TipoSolicitud.BAJA_MATERIA, false);

        assertEquals(Estado.PENDIENTE, s.estado());
        assertFalse(s.tieneResponsable());
        assertEquals(Prioridad.MEDIA, s.prioridad());
    }

    @Test
    public void elFlujoCompletoPendienteEnProcesoResuelta() {
        Solicitud s = nueva(TipoSolicitud.CERTIFICADO, false);
        s.asignarA(Responsable.SECRETARIA);

        s.pasarA(Estado.EN_PROCESO);
        s.pasarA(Estado.RESUELTA);

        assertEquals(Estado.RESUELTA, s.estado());
        assertTrue(s.estaCerrada());
    }

    @Test
    public void unEstadoFinalNoAdmiteMasCambios() {
        Solicitud s = nueva(TipoSolicitud.CERTIFICADO, false);
        s.pasarA(Estado.RECHAZADA);

        SolicitudException e = assertThrows(SolicitudException.class, () -> s.pasarA(Estado.EN_PROCESO));

        assertEquals("transicion invalida RECHAZADA -> EN_PROCESO", e.getMessage());
    }

    @Test
    public void unaSolicitudSePuedeReasignarMientrasEstaAbierta() {
        Solicitud s = nueva(TipoSolicitud.CERTIFICADO, false);

        s.asignarA(Responsable.BEDELIA);
        s.asignarA(Responsable.ALUMNADO);

        assertEquals(Responsable.ALUMNADO, s.responsable());
    }

    @Test
    public void rechazarNoRequiereResponsable() {
        Solicitud s = nueva(TipoSolicitud.CERTIFICADO, false);

        s.pasarA(Estado.RECHAZADA);

        assertEquals(Estado.RECHAZADA, s.estado());
    }

    @Test
    public void laDescripcionNulaSeNormalizaAVacia() {
        NuevaSolicitud datos = new NuevaSolicitud(DatosDePrueba.ana(), TipoSolicitud.CERTIFICADO, null, false);

        assertEquals("", datos.descripcion());
    }
}
