package ar.edu.solicitudes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FiltroSolicitudesTest {

    private Solicitud solicitudDeEjemplo() {
        Solicitud s = new Solicitud();
        s.estudiante = new Estudiante("12345", "Ana Perez", "ana@mail.com");
        s.tipo = TipoSolicitud.CERTIFICADO;
        s.prioridad = Prioridad.BAJA;
        s.estado = Estado.PENDIENTE;
        s.responsable = null;
        return s;
    }

    @Test
    public void unFiltroVacioAceptaTodo() {
        assertTrue(new FiltroSolicitudes("", "", "", "", "").acepta(solicitudDeEjemplo()));
    }

    @Test
    public void filtraPorCadaCriterio() {
        Solicitud s = solicitudDeEjemplo();
        assertTrue(new FiltroSolicitudes("PENDIENTE", "", "", "", "").acepta(s));
        assertFalse(new FiltroSolicitudes("RESUELTA", "", "", "", "").acepta(s));
        assertTrue(new FiltroSolicitudes("", "CERTIFICADO", "", "", "").acepta(s));
        assertFalse(new FiltroSolicitudes("", "EQUIVALENCIA", "", "", "").acepta(s));
        assertTrue(new FiltroSolicitudes("", "", "BAJA", "", "").acepta(s));
        assertFalse(new FiltroSolicitudes("", "", "ALTA", "", "").acepta(s));
        assertTrue(new FiltroSolicitudes("", "", "", "", "12345").acepta(s));
        assertFalse(new FiltroSolicitudes("", "", "", "", "99999").acepta(s));
    }

    @Test
    public void filtraPorResponsableYUnaSolicitudSinResponsableNoCoincideConNinguno() {
        Solicitud s = solicitudDeEjemplo();
        assertFalse(new FiltroSolicitudes("", "", "", "BEDELIA", "").acepta(s));
        s.responsable = Responsable.BEDELIA;
        assertTrue(new FiltroSolicitudes("", "", "", "BEDELIA", "").acepta(s));
        assertFalse(new FiltroSolicitudes("", "", "", "ALUMNADO", "").acepta(s));
    }

    @Test
    public void losCriteriosSeCombinanConYLogico() {
        Solicitud s = solicitudDeEjemplo();
        assertTrue(new FiltroSolicitudes("PENDIENTE", "CERTIFICADO", "", "", "").acepta(s));
        assertFalse(new FiltroSolicitudes("PENDIENTE", "EQUIVALENCIA", "", "", "").acepta(s));
    }
}
