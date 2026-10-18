package ar.edu.solicitudes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class EnumsDominioTest {

    @Test
    public void desdeDevuelveLaConstanteConElMismoNombre() {
        assertSame(Estado.EN_PROCESO, Estado.desde("EN_PROCESO"));
        assertSame(Prioridad.MEDIA, Prioridad.desde("MEDIA"));
        assertSame(TipoSolicitud.BAJA_MATERIA, TipoSolicitud.desde("BAJA_MATERIA"));
        assertSame(Responsable.SECRETARIA, Responsable.desde("SECRETARIA"));
    }

    @Test
    public void desdeDevuelveNullSiElTextoNoExiste() {
        assertNull(Estado.desde("CANCELADA"));
        assertNull(Prioridad.desde(""));
        assertNull(TipoSolicitud.desde("PASAPORTE"));
        assertNull(Responsable.desde("RECTORIA"));
    }

    @Test
    public void desdeDistingueMayusculasDeMinusculas() {
        // la normalizacion a mayusculas es responsabilidad de quien lee la entrada (la consola)
        assertNull(Estado.desde("pendiente"));
    }

    @Test
    public void elOrdenDeDeclaracionDefineElOrdenDelReporte() {
        assertEquals("[PENDIENTE, EN_PROCESO, RESUELTA, RECHAZADA]", java.util.Arrays.toString(Estado.values()));
        assertEquals("[ALTA, MEDIA, BAJA]", java.util.Arrays.toString(Prioridad.values()));
    }

    @Test
    public void opcionesListaLosNombresSeparadosPorBarra() {
        assertEquals("CERTIFICADO/EQUIVALENCIA/REVISION_NOTA/BAJA_MATERIA", Enums.opciones(TipoSolicitud.class));
        assertEquals("BEDELIA/ALUMNADO/SECRETARIA", Enums.opciones(Responsable.class));
    }
}
