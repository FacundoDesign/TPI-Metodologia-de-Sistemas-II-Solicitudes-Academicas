package ar.edu.solicitudes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Mejora de manejo de errores (Replace Error Code with Exception): la version inicial ignoraba en silencio un
 * archivo de datos danado y seguia con datos incompletos. Ahora avisa y termina con codigo 1.
 * Esto NO forma parte del contrato de la version inicial: es una mejora deliberada y documentada.
 */
public class ArchivoDatosCorruptoTest {

    private Path trabajo;
    private Path entrada;

    @Before
    public void preparar() throws Exception {
        trabajo = Files.createTempDirectory("solicitudes-corrupto-");
        entrada = trabajo.resolve("entrada.txt");
        Files.writeString(entrada, "0\n", StandardCharsets.UTF_8);
    }

    @After
    public void limpiar() throws Exception {
        try (var paths = Files.walk(trabajo)) {
            paths.sorted((a, b) -> b.compareTo(a)).forEach(p -> p.toFile().delete());
        }
    }

    @Test
    public void unaLineaConCantidadDeCamposIncorrectaDetieneElArranque() throws Exception {
        Files.writeString(trabajo.resolve("solicitudes.csv"), "1;12345;Ana\n", StandardCharsets.UTF_8);
        ProcesoAplicacion.Resultado r = ProcesoAplicacion.ejecutar(trabajo, entrada);
        assertEquals(1, r.codigo());
        assertTrue(r.errores(), r.errores().contains("linea 1"));
        assertTrue(r.errores(), r.errores().contains("se esperaban 10 campos"));
        assertEquals("", r.salida());
    }

    @Test
    public void unEstadoDesconocidoEnElArchivoDetieneElArranque() throws Exception {
        Files.writeString(trabajo.resolve("solicitudes.csv"),
                "1;12345;Ana Perez;ana@mail.com;CERTIFICADO;desc;BAJA;COLGADA;-;0\n", StandardCharsets.UTF_8);
        ProcesoAplicacion.Resultado r = ProcesoAplicacion.ejecutar(trabajo, entrada);
        assertEquals(1, r.codigo());
        assertTrue(r.errores(), r.errores().contains("tipo, prioridad o estado desconocido"));
    }

    @Test
    public void unArchivoValidoArrancaNormalmente() throws Exception {
        Files.writeString(trabajo.resolve("solicitudes.csv"),
                "1;12345;Ana Perez;ana@mail.com;CERTIFICADO;desc;BAJA;PENDIENTE;-;0\n", StandardCharsets.UTF_8);
        ProcesoAplicacion.Resultado r = ProcesoAplicacion.ejecutar(trabajo, entrada);
        assertEquals(0, r.codigo());
        assertTrue(r.salida().contains("Hasta luego"));
    }
}
