package ar.edu.solicitudes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PrioridadTest {

    @Test
    public void subirAumentaUnNivelYAltaEsElTecho() {
        assertEquals(Prioridad.MEDIA, Prioridad.BAJA.subir());
        assertEquals(Prioridad.ALTA, Prioridad.MEDIA.subir());
        assertEquals(Prioridad.ALTA, Prioridad.ALTA.subir());
    }

    @Test
    public void plazoBaseSegunPrioridad() {
        assertEquals(3, Prioridad.ALTA.plazoBaseDias());
        assertEquals(5, Prioridad.MEDIA.plazoBaseDias());
        assertEquals(10, Prioridad.BAJA.plazoBaseDias());
    }
}
