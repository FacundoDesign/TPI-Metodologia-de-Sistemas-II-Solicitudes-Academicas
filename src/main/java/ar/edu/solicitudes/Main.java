package ar.edu.solicitudes;

import java.nio.file.Path;
import java.util.Scanner;

// Punto de entrada y "composition root": es el unico lugar que decide que implementaciones concretas se usan
// (CSV + consola) y las conecta entre si. Nada mas del sistema usa `new` sobre colaboradores.
public class Main {

    private static final Path ARCHIVO_DATOS = Path.of("solicitudes.csv");

    public static void main(String[] args) {
        SolicitudService servicio;
        try {
            servicio = new SolicitudService(new CsvSolicitudRepository(ARCHIVO_DATOS));
        } catch (ArchivoDatosException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
            return;
        }
        new ConsolaMenu(servicio, new Scanner(System.in), System.out).ejecutar();
    }
}
