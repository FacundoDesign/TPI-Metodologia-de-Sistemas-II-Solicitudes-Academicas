package ar.edu.solicitudes;

import java.util.*;

// Refactoring 1 (Extract Method + Rename): main() ya no contiene la logica de cada opcion del menu.
public class Main {

    public static void main(String[] args) {
        GestorSolicitudes.cargar();
        Scanner teclado = new Scanner(System.in);
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            String opcion = teclado.hasNextLine() ? teclado.nextLine().trim() : "0";
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
                salir = true;
            } else {
                System.out.println("Opcion invalida");
            }
        }
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
        int resultado = GestorSolicitudes.registrar(legajo, nombre, email, tipo, descripcion, urgente.equalsIgnoreCase("s"));
        if (resultado == -1) System.out.println("Error: legajo invalido (se esperan 5 digitos)");
        else if (resultado == -2) System.out.println("Error: el nombre no puede estar vacio");
        else if (resultado == -3) System.out.println("Error: email invalido");
        else if (resultado == -4) System.out.println("Error: tipo de solicitud desconocido");
        else {
            Solicitud nueva = GestorSolicitudes.buscarPorId(resultado);
            System.out.println("Solicitud registrada con ID " + resultado + " (prioridad " + nueva.prioridad
                    + ", plazo " + GestorSolicitudes.calcularPlazo(nueva) + " dias)");
        }
    }

    private static void cambiarEstado(Scanner teclado) {
        Integer id = leerId(teclado);
        if (id == null) return;
        String nuevoEstado = leerTextoEnMayusculas(teclado, "Nuevo estado: ");
        int resultado = GestorSolicitudes.cambiarEstado(id, nuevoEstado);
        if (resultado == 0) System.out.println("Estado actualizado: #" + id + " -> " + nuevoEstado);
        else if (resultado == -1) System.out.println("Error: no existe la solicitud " + id);
        else if (resultado == -2) System.out.println("Error: estado desconocido");
        else if (resultado == -3) {
            Solicitud s = GestorSolicitudes.buscarPorId(id);
            System.out.println("Error: transicion invalida " + s.estado + " -> " + nuevoEstado);
        } else if (resultado == -4) System.out.println("Error: debe asignar responsable antes de pasar a EN_PROCESO");
    }

    private static void asignarResponsable(Scanner teclado) {
        Integer id = leerId(teclado);
        if (id == null) return;
        String responsable = leerTextoEnMayusculas(teclado, "Responsable (BEDELIA/ALUMNADO/SECRETARIA): ");
        int resultado = GestorSolicitudes.asignarResponsable(id, responsable);
        if (resultado == 0) System.out.println("Responsable asignado: #" + id + " -> " + responsable);
        else if (resultado == -1) System.out.println("Error: no existe la solicitud " + id);
        else if (resultado == -2) System.out.println("Error: responsable desconocido");
        else if (resultado == -3) System.out.println("Error: la solicitud esta cerrada");
    }

    private static void buscarSolicitudes(Scanner teclado) {
        String estado = leerTextoEnMayusculas(teclado, "Filtro estado (vacio = todos): ");
        String tipo = leerTextoEnMayusculas(teclado, "Filtro tipo (vacio = todos): ");
        String prioridad = leerTextoEnMayusculas(teclado, "Filtro prioridad (vacio = todos): ");
        String responsable = leerTextoEnMayusculas(teclado, "Filtro responsable (vacio = todos): ");
        String legajo = leerTexto(teclado, "Filtro legajo (vacio = todos): ");
        List<Solicitud> resultado = GestorSolicitudes.buscar(estado, tipo, prioridad, responsable, legajo);
        if (resultado.size() == 0) {
            System.out.println("Sin resultados");
            return;
        }
        for (int i = 0; i < resultado.size(); i++) {
            System.out.println(formatearFila(resultado.get(i)));
        }
        System.out.println("Total: " + resultado.size() + " solicitud(es)");
    }

    private static String formatearFila(Solicitud s) {
        return "#" + s.id + " | " + s.legajo + " | " + s.nombre + " | " + s.tipo + " | "
                + s.prioridad + " | " + s.estado + " | "
                + (s.responsable == null ? "sin asignar" : s.responsable);
    }

    // ------------------------------------------------------------------ entrada de datos

    private static String leerTexto(Scanner teclado, String consigna) {
        System.out.print(consigna);
        return teclado.nextLine().trim();
    }

    private static String leerTextoEnMayusculas(Scanner teclado, String consigna) {
        return leerTexto(teclado, consigna).toUpperCase();
    }

    // devuelve null (y avisa) si lo ingresado no es un numero
    private static Integer leerId(Scanner teclado) {
        String texto = leerTexto(teclado, "ID: ");
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            System.out.println("Error: ID invalido");
            return null;
        }
    }
}
