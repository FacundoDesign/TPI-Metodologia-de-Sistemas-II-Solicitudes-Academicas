package ar.edu.solicitudes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Utilidad de pruebas: ejecuta la aplicacion como proceso externo (igual que una persona usuaria) dentro de un
 * directorio de trabajo dado y devuelve lo que escribio en pantalla, en el canal de errores y su codigo de salida.
 */
final class ProcesoAplicacion {

    record Resultado(String salida, String errores, int codigo) {
    }

    private ProcesoAplicacion() {
    }

    static Resultado ejecutar(Path directorioTrabajo, Path archivoDeEntrada) throws IOException, InterruptedException {
        String ejecutableJava = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        ProcessBuilder pb = new ProcessBuilder(ejecutableJava, "-cp", classpathAbsoluto(), "ar.edu.solicitudes.Main");
        pb.directory(directorioTrabajo.toFile());
        pb.redirectInput(archivoDeEntrada.toFile());
        Path errores = directorioTrabajo.resolve(".stderr");
        pb.redirectError(errores.toFile());
        Process p = pb.start();
        byte[] salida = p.getInputStream().readAllBytes();
        if (!p.waitFor(30, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new IllegalStateException("El programa no termino a tiempo");
        }
        String textoErrores = new String(Files.readAllBytes(errores), StandardCharsets.UTF_8);
        Files.delete(errores);
        return new Resultado(new String(salida, StandardCharsets.UTF_8), textoErrores, p.exitValue());
    }

    // El proceso hijo corre en otro directorio: el classpath tiene que ser absoluto.
    private static String classpathAbsoluto() {
        StringBuilder sb = new StringBuilder();
        for (String entrada : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            sb.append(new File(entrada).getAbsolutePath());
        }
        return sb.toString();
    }
}
