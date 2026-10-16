package ar.edu.solicitudes;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        GestorSolicitudes.cargar();
        Scanner sc = new Scanner(System.in);
        boolean salir = false;
        while (!salir) {
            System.out.println("=== GESTION DE SOLICITUDES ACADEMICAS ===");
            System.out.println("1. Registrar solicitud");
            System.out.println("2. Cambiar estado");
            System.out.println("3. Asignar responsable");
            System.out.println("4. Buscar / filtrar");
            System.out.println("5. Reporte");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            String op = sc.hasNextLine() ? sc.nextLine().trim() : "0";
            if (op.equals("1")) {
                System.out.print("Legajo: ");
                String l = sc.nextLine().trim();
                System.out.print("Nombre: ");
                String n = sc.nextLine().trim();
                System.out.print("Email: ");
                String e = sc.nextLine().trim();
                System.out.print("Tipo (CERTIFICADO/EQUIVALENCIA/REVISION_NOTA/BAJA_MATERIA): ");
                String t = sc.nextLine().trim().toUpperCase();
                System.out.print("Descripcion: ");
                String d = sc.nextLine().trim();
                System.out.print("Urgente (s/n): ");
                String u = sc.nextLine().trim();
                int r = GestorSolicitudes.registrar(l, n, e, t, d, u.equalsIgnoreCase("s"));
                if (r == -1) System.out.println("Error: legajo invalido (se esperan 5 digitos)");
                else if (r == -2) System.out.println("Error: el nombre no puede estar vacio");
                else if (r == -3) System.out.println("Error: email invalido");
                else if (r == -4) System.out.println("Error: tipo de solicitud desconocido");
                else {
                    Solicitud s = GestorSolicitudes.buscarPorId(r);
                    System.out.println("Solicitud registrada con ID " + r + " (prioridad " + s.prioridad
                            + ", plazo " + GestorSolicitudes.calcularPlazo(s) + " dias)");
                }
            } else if (op.equals("2")) {
                System.out.print("ID: ");
                String sid = sc.nextLine();
                int id;
                try {
                    id = Integer.parseInt(sid.trim());
                } catch (Exception ex) {
                    System.out.println("Error: ID invalido");
                    continue;
                }
                System.out.print("Nuevo estado: ");
                String ne = sc.nextLine().trim().toUpperCase();
                int r = GestorSolicitudes.cambiarEstado(id, ne);
                if (r == 0) System.out.println("Estado actualizado: #" + id + " -> " + ne);
                else if (r == -1) System.out.println("Error: no existe la solicitud " + id);
                else if (r == -2) System.out.println("Error: estado desconocido");
                else if (r == -3) {
                    Solicitud s = GestorSolicitudes.buscarPorId(id);
                    System.out.println("Error: transicion invalida " + s.estado + " -> " + ne);
                } else if (r == -4) System.out.println("Error: debe asignar responsable antes de pasar a EN_PROCESO");
            } else if (op.equals("3")) {
                System.out.print("ID: ");
                String sid = sc.nextLine();
                int id;
                try {
                    id = Integer.parseInt(sid.trim());
                } catch (Exception ex) {
                    System.out.println("Error: ID invalido");
                    continue;
                }
                System.out.print("Responsable (BEDELIA/ALUMNADO/SECRETARIA): ");
                String re = sc.nextLine().trim().toUpperCase();
                int r = GestorSolicitudes.asignarResponsable(id, re);
                if (r == 0) System.out.println("Responsable asignado: #" + id + " -> " + re);
                else if (r == -1) System.out.println("Error: no existe la solicitud " + id);
                else if (r == -2) System.out.println("Error: responsable desconocido");
                else if (r == -3) System.out.println("Error: la solicitud esta cerrada");
            } else if (op.equals("4")) {
                System.out.print("Filtro estado (vacio = todos): ");
                String fe = sc.nextLine().trim().toUpperCase();
                System.out.print("Filtro tipo (vacio = todos): ");
                String ft = sc.nextLine().trim().toUpperCase();
                System.out.print("Filtro prioridad (vacio = todos): ");
                String fp = sc.nextLine().trim().toUpperCase();
                System.out.print("Filtro responsable (vacio = todos): ");
                String fr = sc.nextLine().trim().toUpperCase();
                System.out.print("Filtro legajo (vacio = todos): ");
                String fl = sc.nextLine().trim();
                List<Solicitud> res = GestorSolicitudes.buscar(fe, ft, fp, fr, fl);
                if (res.size() == 0) {
                    System.out.println("Sin resultados");
                } else {
                    for (int i = 0; i < res.size(); i++) {
                        Solicitud s = res.get(i);
                        System.out.println("#" + s.id + " | " + s.legajo + " | " + s.nombre + " | " + s.tipo + " | "
                                + s.prioridad + " | " + s.estado + " | "
                                + (s.responsable == null ? "sin asignar" : s.responsable));
                    }
                    System.out.println("Total: " + res.size() + " solicitud(es)");
                }
            } else if (op.equals("5")) {
                GestorSolicitudes.imprimirReporte();
            } else if (op.equals("0")) {
                System.out.println("Hasta luego");
                salir = true;
            } else {
                System.out.println("Opcion invalida");
            }
        }
    }
}
