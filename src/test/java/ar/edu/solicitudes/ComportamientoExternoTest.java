package ar.edu.solicitudes;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Red de seguridad del refactoring (pruebas de caracterizacion / "golden master").
 *
 * Cada escenario es una carpeta en src/test/resources/golden con pasos stepN.in (lo que "teclea" el usuario)
 * y stepN.out (la salida que produjo la VERSION INICIAL, tag estado-inicial). El programa se ejecuta como
 * proceso externo, igual que lo usaria una persona, dentro de un directorio temporal. Los pasos de un
 * mismo escenario comparten directorio, por lo que tambien se verifica la persistencia entre ejecuciones.
 *
 * Mientras estas pruebas pasen, el comportamiento externo no cambio: es la definicion operativa de
 * "refactoring seguro".
 */
public class ComportamientoExternoTest {

    private static final String[] ESCENARIOS = {
        "01-registro-y-prioridades",
        "02-validaciones-registro",
        "03-flujo-estados",
        "04-busqueda-filtros",
        "05-reporte-vacio",
        "06-persistencia"
    };

    @Test
    public void registroYPrioridades() throws Exception {
        verificar("01-registro-y-prioridades");
    }

    @Test
    public void validacionesDeRegistro() throws Exception {
        verificar("02-validaciones-registro");
    }

    @Test
    public void flujoDeEstadosYAsignaciones() throws Exception {
        verificar("03-flujo-estados");
    }

    @Test
    public void busquedaConFiltros() throws Exception {
        verificar("04-busqueda-filtros");
    }

    @Test
    public void reporteSinDatos() throws Exception {
        verificar("05-reporte-vacio");
    }

    @Test
    public void persistenciaEntreEjecuciones() throws Exception {
        verificar("06-persistencia");
    }

    @Test
    public void todosLosEscenariosEstanRegistrados() throws Exception {
        Path raiz = recursosGolden();
        List<String> enDisco = new ArrayList<>();
        try (var dirs = Files.list(raiz)) {
            dirs.filter(Files::isDirectory).forEach(d -> enDisco.add(d.getFileName().toString()));
        }
        enDisco.sort(String::compareTo);
        assertEquals(Arrays.asList(ESCENARIOS), enDisco);
    }

    // ---------------------------------------------------------------- infraestructura

    private void verificar(String escenario) throws Exception {
        Path dir = recursosGolden().resolve(escenario);
        Path trabajo = Files.createTempDirectory("solicitudes-golden-");
        try {
            List<Path> pasos = new ArrayList<>();
            try (var s = Files.list(dir)) {
                s.filter(p -> p.getFileName().toString().endsWith(".in")).forEach(pasos::add);
            }
            pasos.sort(Path::compareTo);
            for (Path entrada : pasos) {
                String nombre = entrada.getFileName().toString().replace(".in", "");
                String esperado = Files.readString(dir.resolve(nombre + ".out"), StandardCharsets.UTF_8);
                String obtenido = ejecutar(trabajo, entrada);
                assertEquals("Escenario " + escenario + " / " + nombre, esperado, obtenido);
            }
        } finally {
            borrar(trabajo);
        }
    }

    private String ejecutar(Path directorioTrabajo, Path entrada) throws IOException, InterruptedException {
        ProcesoAplicacion.Resultado r = ProcesoAplicacion.ejecutar(directorioTrabajo, entrada);
        assertEquals("El programa debe terminar con codigo 0", 0, r.codigo());
        return r.salida();
    }

    private Path recursosGolden() throws Exception {
        // Se busca primero en el classpath (Maven/scripts) y si no, en el arbol de fuentes.
        var url = getClass().getClassLoader().getResource("golden");
        if (url != null && "file".equals(url.getProtocol())) {
            return Path.of(url.toURI());
        }
        return Path.of("src", "test", "resources", "golden");
    }

    private void borrar(Path dir) throws IOException {
        try (var paths = Files.walk(dir)) {
            paths.sorted((a, b) -> b.compareTo(a)).map(Path::toFile).forEach(File::delete);
        }
    }
}
