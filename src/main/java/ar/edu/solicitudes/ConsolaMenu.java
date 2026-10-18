package ar.edu.solicitudes;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

// Interfaz de consola (SRP): lee datos, llama al servicio y formatea la salida. No contiene reglas de negocio.
// El orden en que se validan los datos (p. ej. que la solicitud exista antes de interpretar el estado) es parte
// del comportamiento externo y esta cubierto por los escenarios de referencia.
public class ConsolaMenu {

    private final SolicitudService servicio;
    private final Scanner teclado;
    private final PrintStream salida;

    public ConsolaMenu(SolicitudService servicio, Scanner teclado, PrintStream salida) {
        this.servicio = servicio;
        this.teclado = teclado;
        this.salida = salida;
    }

    public void ejecutar() {
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            String opcion = teclado.hasNextLine() ? teclado.nextLine().trim() : "0";
            try {
                salir = ejecutarOpcion(opcion);
            } catch (SolicitudException | ArchivoDatosException e) {
                salida.println("Error: " + e.getMessage());
            }
        }
    }

    // devuelve true si la persona eligio salir
    private boolean ejecutarOpcion(String opcion) {
        switch (opcion) {
            case "1" -> registrarSolicitud();
            case "2" -> cambiarEstado();
            case "3" -> asignarResponsable();
            case "4" -> buscarSolicitudes();
            case "5" -> mostrarReporte();
            case "0" -> {
                salida.println("Hasta luego");
                return true;
            }
            default -> salida.println("Opcion invalida");
        }
        return false;
    }

    private void mostrarMenu() {
        salida.println("=== GESTION DE SOLICITUDES ACADEMICAS ===");
        salida.println("1. Registrar solicitud");
        salida.println("2. Cambiar estado");
        salida.println("3. Asignar responsable");
        salida.println("4. Buscar / filtrar");
        salida.println("5. Reporte");
        salida.println("0. Salir");
        salida.print("Opcion: ");
    }

    private void registrarSolicitud() {
        String legajo = leerTexto("Legajo: ");
        String nombre = leerTexto("Nombre: ");
        String email = leerTexto("Email: ");
        String textoTipo = leerTextoEnMayusculas("Tipo (" + Enums.opciones(TipoSolicitud.class) + "): ");
        String descripcion = leerTexto("Descripcion: ");
        boolean urgente = leerTexto("Urgente (s/n): ").equalsIgnoreCase("s");
        Estudiante estudiante = new Estudiante(legajo, nombre, email);
        TipoSolicitud tipo = requerir(TipoSolicitud.desde(textoTipo), "tipo de solicitud desconocido");
        Solicitud nueva = servicio.registrar(new NuevaSolicitud(estudiante, tipo, descripcion, urgente));
        salida.println("Solicitud registrada con ID " + nueva.id() + " (prioridad " + nueva.prioridad()
                + ", plazo " + nueva.calcularPlazo() + " dias)");
    }

    private void cambiarEstado() {
        int id = leerId();
        String textoEstado = leerTextoEnMayusculas("Nuevo estado: ");
        servicio.obtener(id);
        Estado nuevoEstado = requerir(Estado.desde(textoEstado), "estado desconocido");
        servicio.cambiarEstado(id, nuevoEstado);
        salida.println("Estado actualizado: #" + id + " -> " + textoEstado);
    }

    private void asignarResponsable() {
        int id = leerId();
        String textoResponsable = leerTextoEnMayusculas("Responsable (" + Enums.opciones(Responsable.class) + "): ");
        servicio.obtener(id);
        Responsable responsable = requerir(Responsable.desde(textoResponsable), "responsable desconocido");
        servicio.asignarResponsable(id, responsable);
        salida.println("Responsable asignado: #" + id + " -> " + textoResponsable);
    }

    private void buscarSolicitudes() {
        String estado = leerTextoEnMayusculas("Filtro estado (vacio = todos): ");
        String tipo = leerTextoEnMayusculas("Filtro tipo (vacio = todos): ");
        String prioridad = leerTextoEnMayusculas("Filtro prioridad (vacio = todos): ");
        String responsable = leerTextoEnMayusculas("Filtro responsable (vacio = todos): ");
        String legajo = leerTexto("Filtro legajo (vacio = todos): ");
        List<Solicitud> resultado = servicio.buscar(new FiltroSolicitudes(estado, tipo, prioridad, responsable, legajo));
        if (resultado.isEmpty()) {
            salida.println("Sin resultados");
            return;
        }
        for (Solicitud s : resultado) {
            salida.println(formatearFila(s));
        }
        salida.println("Total: " + resultado.size() + " solicitud(es)");
    }

    private void mostrarReporte() {
        ReporteSolicitudes reporte = ReporteSolicitudes.generar(servicio.todas());
        salida.println("--- REPORTE ---");
        salida.println("Total de solicitudes: " + reporte.total());
        salida.println("Por estado:");
        reporte.porEstado().forEach((estado, cantidad) -> salida.println("  " + estado + ": " + cantidad));
        salida.println("Por prioridad:");
        reporte.porPrioridad().forEach((prioridad, cantidad) -> salida.println("  " + prioridad + ": " + cantidad));
        salida.println("Sin responsable asignado: " + reporte.sinResponsable());
        salida.println("Plazo promedio de respuesta: " + reporte.plazoPromedioDias() + " dias");
    }

    private String formatearFila(Solicitud s) {
        return "#" + s.id() + " | " + s.estudiante().legajo() + " | " + s.estudiante().nombre() + " | " + s.tipo() + " | "
                + s.prioridad() + " | " + s.estado() + " | "
                + (s.tieneResponsable() ? s.responsable().name() : "sin asignar");
    }

    // ------------------------------------------------------------------ entrada de datos

    private String leerTexto(String consigna) {
        salida.print(consigna);
        return teclado.nextLine().trim();
    }

    private String leerTextoEnMayusculas(String consigna) {
        return leerTexto(consigna).toUpperCase();
    }

    private int leerId() {
        String texto = leerTexto("ID: ");
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new SolicitudException("ID invalido");
        }
    }

    private static <T> T requerir(T valor, String mensajeSiFalta) {
        if (valor == null) throw new SolicitudException(mensajeSiFalta);
        return valor;
    }
}
