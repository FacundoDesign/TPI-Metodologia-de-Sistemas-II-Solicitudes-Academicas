package ar.edu.solicitudes;

import java.util.*;

// Refactoring 1 (Extract Method + Rename): main() ya no contiene la logica de cada opcion del menu.
// Refactoring 2 (Replace Type Code with Enum): usa los enums del dominio en lugar de strings.
// Refactoring 4 (Introduce Parameter Object): arma NuevaSolicitud y FiltroSolicitudes.
// Refactoring 5 (Replace Error Code with Exception): un unico catch muestra todos los errores.
public class Main {

    public static void main(String[] args) {
        try {
            GestorSolicitudes.cargar();
        } catch (ArchivoDatosException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
        Scanner teclado = new Scanner(System.in);
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            String opcion = teclado.hasNextLine() ? teclado.nextLine().trim() : "0";
            try {
                salir = ejecutarOpcion(opcion, teclado);
            } catch (SolicitudException | ArchivoDatosException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // devuelve true si la persona eligio salir
    private static boolean ejecutarOpcion(String opcion, Scanner teclado) {
        if (opcion.equals("1")) {
            registrarSolicitud(teclado);
        } else if (opcion.equals("2")) {
            cambiarEstado(teclado);
        } else if (opcion.equals("3")) {
            asignarResponsable(teclado);
        } else if (opcion.equals("4")) {
            buscarSolicitudes(teclado);
        } else if (opcion.equals("5")) {
            GestorSolicitudes.imprimirReporte();
        } else if (opcion.equals("0")) {
            System.out.println("Hasta luego");
            return true;
        } else {
            System.out.println("Opcion invalida");
        }
        return false;
    }

    private static void mostrarMenu() {
        System.out.println("=== GESTION DE SOLICITUDES ACADEMICAS ===");
        System.out.println("1. Registrar solicitud");
        System.out.println("2. Cambiar estado");
        System.out.println("3. Asignar responsable");
        System.out.println("4. Buscar / filtrar");
        System.out.println("5. Reporte");
        System.out.println("0. Salir");
        System.out.print("Opcion: ");
    }

    private static void registrarSolicitud(Scanner teclado) {
        String legajo = leerTexto(teclado, "Legajo: ");
        String nombre = leerTexto(teclado, "Nombre: ");
        String email = leerTexto(teclado, "Email: ");
        String tipo = leerTextoEnMayusculas(teclado, "Tipo (CERTIFICADO/EQUIVALENCIA/REVISION_NOTA/BAJA_MATERIA): ");
        String descripcion = leerTexto(teclado, "Descripcion: ");
        String urgente = leerTexto(teclado, "Urgente (s/n): ");
        NuevaSolicitud datos = new NuevaSolicitud(new Estudiante(legajo, nombre, email), tipo, descripcion, urgente.equalsIgnoreCase("s"));
        Solicitud nueva = GestorSolicitudes.registrar(datos);
        System.out.println("Solicitud registrada con ID " + nueva.id + " (prioridad " + nueva.prioridad
                + ", plazo " + nueva.calcularPlazo() + " dias)");
    }

    private static void cambiarEstado(Scanner teclado) {
        int id = leerId(teclado);
        String nuevoEstado = leerTextoEnMayusculas(teclado, "Nuevo estado: ");
        GestorSolicitudes.cambiarEstado(id, nuevoEstado);
        System.out.println("Estado actualizado: #" + id + " -> " + nuevoEstado);
    }

    private static void asignarResponsable(Scanner teclado) {
        int id = leerId(teclado);
        String responsable = leerTextoEnMayusculas(teclado, "Responsable (BEDELIA/ALUMNADO/SECRETARIA): ");
        GestorSolicitudes.asignarResponsable(id, responsable);
        System.out.println("Responsable asignado: #" + id + " -> " + responsable);
    }

    private static void buscarSolicitudes(Scanner teclado) {
        String estado = leerTextoEnMayusculas(teclado, "Filtro estado (vacio = todos): ");
        String tipo = leerTextoEnMayusculas(teclado, "Filtro tipo (vacio = todos): ");
        String prioridad = leerTextoEnMayusculas(teclado, "Filtro prioridad (vacio = todos): ");
        String responsable = leerTextoEnMayusculas(teclado, "Filtro responsable (vacio = todos): ");
        String legajo = leerTexto(teclado, "Filtro legajo (vacio = todos): ");
        List<Solicitud> resultado = GestorSolicitudes.buscar(new FiltroSolicitudes(estado, tipo, prioridad, responsable, legajo));
        if (resultado.isEmpty()) {
            System.out.println("Sin resultados");
            return;
        }
        for (Solicitud s : resultado) {
            System.out.println(formatearFila(s));
        }
        System.out.println("Total: " + resultado.size() + " solicitud(es)");
    }

    private static String formatearFila(Solicitud s) {
        return "#" + s.id + " | " + s.estudiante.legajo() + " | " + s.estudiante.nombre() + " | " + s.tipo + " | "
                + s.prioridad + " | " + s.estado + " | "
                + (s.responsable == null ? "sin asignar" : s.responsable.name());
    }

    // ------------------------------------------------------------------ entrada de datos

    private static String leerTexto(Scanner teclado, String consigna) {
        System.out.print(consigna);
        return teclado.nextLine().trim();
    }

    private static String leerTextoEnMayusculas(Scanner teclado, String consigna) {
        return leerTexto(teclado, consigna).toUpperCase();
    }

    private static int leerId(Scanner teclado) {
        String texto = leerTexto(teclado, "ID: ");
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new SolicitudException("ID invalido");
        }
    }
}
