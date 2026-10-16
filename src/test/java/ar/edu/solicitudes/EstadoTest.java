package ar.edu.solicitudes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Test;

public class EstadoTest {

    @Test
    public void soloRESUELTAyRECHAZADAsonEstadosFinales() {
        assertFalse(Estado.PENDIENTE.esFinal());
        assertFalse(Estado.EN_PROCESO.esFinal());
        assertTrue(Estado.RESUELTA.esFinal());
        assertTrue(Estado.RECHAZADA.esFinal());
    }

    @Test
    public void desdePendientePuedeIrAEnProcesoORechazada() {
        assertEquals(EnumSet.of(Estado.EN_PROCESO, Estado.RECHAZADA), destinosDesde(Estado.PENDIENTE));
    }

    @Test
    public void desdeEnProcesoPuedeIrAResueltaORechazada() {
        assertEquals(EnumSet.of(Estado.RESUELTA, Estado.RECHAZADA), destinosDesde(Estado.EN_PROCESO));
    }

    @Test
    public void losEstadosFinalesNoTienenTransiciones() {
        assertTrue(destinosDesde(Estado.RESUELTA).isEmpty());
        assertTrue(destinosDesde(Estado.RECHAZADA).isEmpty());
    }

    @Test
    public void ningunEstadoPuedePasarASiMismo() {
        for (Estado e : Estado.values()) {
            assertFalse(e + " -> " + e, e.puedePasarA(e));
        }
    }

    private Set<Estado> destinosDesde(Estado origen) {
        Set<Estado> destinos = EnumSet.noneOf(Estado.class);
        for (Estado destino : Estado.values()) {
            if (origen.puedePasarA(destino)) {
                destinos.add(destino);
            }
        }
        return destinos;
    }
}
