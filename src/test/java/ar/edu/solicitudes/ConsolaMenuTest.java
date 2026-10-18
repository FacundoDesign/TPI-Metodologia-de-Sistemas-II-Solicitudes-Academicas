package ar.edu.solicitudes;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.Test;

/** Pruebas de la interfaz de consola en el mismo proceso: entrada simulada y salida capturada. */
public class ConsolaMenuTest {

    private String ejecutarConEntrada(String... lineas) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        SolicitudService servicio = new SolicitudService(new RepositorioEnMemoria());
        Scanner teclado = new Scanner(String.join("\n", lineas) + "\n");
        new ConsolaMenu(servicio, teclado, salida).ejecutar();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    public void salirSaludaYTermina() {
        assertTrue(ejecutarConEntrada("0").contains("Hasta luego"));
    }

    @Test
    public void unaOpcionDesconocidaSeInformaYElMenuContinua() {
        String salida = ejecutarConEntrada("9", "0");

        assertTrue(salida.contains("Opcion invalida"));
        assertTrue(salida.contains("Hasta luego"));
    }

    @Test
    public void registrarMuestraIdPrioridadYPlazo() {
        String salida = ejecutarConEntrada("1", "12345", "Ana Perez", "ana@mail.com", "certificado", "constancia", "n", "0");

        assertTrue(salida, salida.contains("Solicitud registrada con ID 1 (prioridad BAJA, plazo 10 dias)"));
    }

    @Test
    public void unErrorDeValidacionSeMuestraSinCortarElProgramaNiPedirMasDatos() {
        String salida = ejecutarConEntrada("1", "123", "Ana Perez", "ana@mail.com", "CERTIFICADO", "x", "n", "0");

        assertTrue(salida, salida.contains("Error: legajo invalido (se esperan 5 digitos)"));
        assertTrue(salida.contains("Hasta luego"));
    }

    @Test
    public void unIdNoNumericoSeInforma() {
        assertTrue(ejecutarConEntrada("2", "abc", "0").contains("Error: ID invalido"));
    }

    @Test
    public void laExistenciaDeLaSolicitudSeValidaAntesQueElEstado() {
        assertTrue(ejecutarConEntrada("2", "99", "XXX", "0").contains("Error: no existe la solicitud 99"));
    }

    @Test
    public void elReporteSinDatosMuestraCeros() {
        String salida = ejecutarConEntrada("5", "0");

        assertTrue(salida.contains("Total de solicitudes: 0"));
        assertTrue(salida.contains("Plazo promedio de respuesta: 0.0 dias"));
    }

    @Test
    public void buscarSinResultadosLoDice() {
        assertTrue(ejecutarConEntrada("4", "", "", "", "", "", "0").contains("Sin resultados"));
    }
}
