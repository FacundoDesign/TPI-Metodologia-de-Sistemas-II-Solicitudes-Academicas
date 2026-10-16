package ar.edu.solicitudes;

import java.io.*;
import java.util.*;

// Gestor de solicitudes: carga, guarda, valida, calcula y reporta.
// Refactoring 1 (Extract Method + Rename): se descompusieron los metodos largos y se eliminaron
// duplicaciones y codigo muerto. La estructura y los codigos de error todavia no cambian.
public class GestorSolicitudes {

    public static List<Solicitud> solicitudes = new ArrayList<Solicitud>();
    public static int proximoId = 1;
    static String ARCHIVO_DATOS = "solicitudes.csv";

    // ------------------------------------------------------------------ persistencia

    public static void cargar() {
        try {
            File archivo = new File(ARCHIVO_DATOS);
            if (!archivo.exists()) return;
            BufferedReader lector = new BufferedReader(new FileReader(archivo));
            String linea;
            while ((linea = lector.readLine()) != null) {
                Solicitud s = parsearLinea(linea);
                solicitudes.add(s);
                if (s.id >= proximoId) proximoId = s.id + 1;
            }
            lector.close();
        } catch (Exception e) {
        }
    }

    public static void guardar() {
        try {
            PrintWriter escritor = new PrintWriter(new FileWriter(ARCHIVO_DATOS));
            for (int i = 0; i < solicitudes.size(); i++) {
                escritor.println(serializar(solicitudes.get(i)));
            }
            escritor.close();
        } catch (Exception e) {
        }
    }

    private static Solicitud parsearLinea(String linea) {
        String[] campos = linea.split(";");
        Solicitud s = new Solicitud();
        s.id = Integer.parseInt(campos[0]);
        s.legajo = campos[1];
        s.nombre = campos[2];
        s.email = campos[3];
        s.tipo = campos[4];
        s.descripcion = campos[5];
        s.prioridad = campos[6];
        s.estado = campos[7];
        s.responsable = campos[8].equals("-") ? null : campos[8];
        s.urgente = campos[9].equals("1");
        return s;
    }

    private static String serializar(Solicitud s) {
        String responsable = s.responsable == null ? "-" : s.responsable;
        String urgente = s.urgente ? "1" : "0";
        return s.id + ";" + s.legajo + ";" + s.nombre + ";" + s.email + ";" + s.tipo + ";"
                + s.descripcion + ";" + s.prioridad + ";" + s.estado + ";" + responsable + ";" + urgente;
    }

    // ------------------------------------------------------------------ casos de uso

    // devuelve el id de la nueva solicitud, o un codigo negativo si hay error
    // -1 legajo, -2 nombre, -3 email, -4 tipo
    public static int registrar(String legajo, String nombre, String email, String tipo, String descripcion, boolean urgente) {
        if (!esLegajoValido(legajo)) return -1;
        if (!esNombreValido(nombre)) return -2;
        if (!esEmailValido(email)) return -3;
        if (!esTipoValido(tipo)) return -4;

        Solicitud nueva = new Solicitud();
        nueva.id = proximoId;
        proximoId++;
        nueva.legajo = legajo;
        nueva.nombre = nombre;
        nueva.email = email;
        nueva.tipo = tipo;
        nueva.descripcion = descripcion == null ? "" : descripcion;
        nueva.prioridad = calcularPrioridad(tipo, urgente);
        nueva.estado = "PENDIENTE";
        nueva.responsable = null;
        nueva.urgente = urgente;
        solicitudes.add(nueva);
        guardar();
        return nueva.id;
    }

    // 0 ok, -1 no existe, -2 estado desconocido, -3 transicion invalida, -4 falta responsable
    public static int cambiarEstado(int id, String nuevoEstado) {
        Solicitud s = buscarPorId(id);
        if (s == null) return -1;
        if (!esEstadoValido(nuevoEstado)) return -2;
        if (!esTransicionPermitida(s.estado, nuevoEstado)) return -3;
        if (nuevoEstado.equals("EN_PROCESO") && s.responsable == null) return -4;
        s.estado = nuevoEstado;
        guardar();
        return 0;
    }

    // 0 ok, -1 no existe, -2 responsable desconocido, -3 solicitud cerrada
    public static int asignarResponsable(int id, String responsable) {
        Solicitud s = buscarPorId(id);
        if (s == null) return -1;
        if (!esResponsableValido(responsable)) return -2;
        if (estaCerrada(s)) return -3;
        s.responsable = responsable;
        guardar();
        return 0;
    }

    public static Solicitud buscarPorId(int id) {
        Solicitud encontrada = null;
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).id == id) {
                encontrada = solicitudes.get(i);
            }
        }
        return encontrada;
    }

    public static List<Solicitud> buscar(String estado, String tipo, String prioridad, String responsable, String legajo) {
        List<Solicitud> resultado = new ArrayList<Solicitud>();
        for (int i = 0; i < solicitudes.size(); i++) {
            Solicitud s = solicitudes.get(i);
            if (cumpleFiltros(s, estado, tipo, prioridad, responsable, legajo)) resultado.add(s);
        }
        return resultado;
    }

    private static boolean cumpleFiltros(Solicitud s, String estado, String tipo, String prioridad, String responsable, String legajo) {
        String responsableActual = s.responsable == null ? "" : s.responsable;
        return coincide(estado, s.estado)
                && coincide(tipo, s.tipo)
                && coincide(prioridad, s.prioridad)
                && coincide(responsable, responsableActual)
                && coincide(legajo, s.legajo);
    }

    // un filtro vacio significa "todos"
    private static boolean coincide(String filtro, String valor) {
        return filtro.equals("") || valor.equals(filtro);
    }

    // ------------------------------------------------------------------ reglas de negocio

    private static boolean esLegajoValido(String legajo) {
        if (legajo == null || legajo.length() != 5) return false;
        for (int i = 0; i < legajo.length(); i++) {
            if (!Character.isDigit(legajo.charAt(i))) return false;
        }
        return true;
    }

    private static boolean esNombreValido(String nombre) {
        return nombre != null && !nombre.trim().equals("");
    }

    private static boolean esEmailValido(String email) {
        return email != null && email.indexOf("@") >= 1 && email.lastIndexOf(".") >= email.indexOf("@");
    }

    private static boolean esTipoValido(String tipo) {
        return tipo.equals("CERTIFICADO") || tipo.equals("EQUIVALENCIA") || tipo.equals("REVISION_NOTA") || tipo.equals("BAJA_MATERIA");
    }

    private static boolean esEstadoValido(String estado) {
        return estado.equals("PENDIENTE") || estado.equals("EN_PROCESO") || estado.equals("RESUELTA") || estado.equals("RECHAZADA");
    }

    private static boolean esResponsableValido(String responsable) {
        return responsable.equals("BEDELIA") || responsable.equals("ALUMNADO") || responsable.equals("SECRETARIA");
    }

    private static boolean estaCerrada(Solicitud s) {
        return s.estado.equals("RESUELTA") || s.estado.equals("RECHAZADA");
    }

    private static boolean esTransicionPermitida(String actual, String nuevo) {
        if (actual.equals("PENDIENTE")) return nuevo.equals("EN_PROCESO") || nuevo.equals("RECHAZADA");
        if (actual.equals("EN_PROCESO")) return nuevo.equals("RESUELTA") || nuevo.equals("RECHAZADA");
        return false;
    }

    private static String calcularPrioridad(String tipo, boolean urgente) {
        String prioridad = prioridadBase(tipo);
        return urgente ? subirPrioridad(prioridad) : prioridad;
    }

    private static String prioridadBase(String tipo) {
        if (tipo.equals("REVISION_NOTA")) return "ALTA";
        if (tipo.equals("EQUIVALENCIA")) return "MEDIA";
        if (tipo.equals("BAJA_MATERIA")) return "MEDIA";
        return "BAJA";
    }

    private static String subirPrioridad(String prioridad) {
        if (prioridad.equals("BAJA")) return "MEDIA";
        if (prioridad.equals("MEDIA")) return "ALTA";
        return prioridad;
    }

    // plazo de respuesta en dias habiles
    public static int calcularPlazo(Solicitud s) {
        int dias;
        if (s.prioridad.equals("ALTA")) dias = 3;
        else if (s.prioridad.equals("MEDIA")) dias = 5;
        else dias = 10;
        if (s.tipo.equals("EQUIVALENCIA")) dias = dias + 5;
        return dias;
    }

    // ------------------------------------------------------------------ reporte

    public static void imprimirReporte() {
        int total = solicitudes.size();
        System.out.println("--- REPORTE ---");
        System.out.println("Total de solicitudes: " + total);
        System.out.println("Por estado:");
        System.out.println("  PENDIENTE: " + contarPorEstado("PENDIENTE"));
        System.out.println("  EN_PROCESO: " + contarPorEstado("EN_PROCESO"));
        System.out.println("  RESUELTA: " + contarPorEstado("RESUELTA"));
        System.out.println("  RECHAZADA: " + contarPorEstado("RECHAZADA"));
        System.out.println("Por prioridad:");
        System.out.println("  ALTA: " + contarPorPrioridad("ALTA"));
        System.out.println("  MEDIA: " + contarPorPrioridad("MEDIA"));
        System.out.println("  BAJA: " + contarPorPrioridad("BAJA"));
        System.out.println("Sin responsable asignado: " + contarSinResponsable());
        System.out.println("Plazo promedio de respuesta: " + calcularPlazoPromedio() + " dias");
    }

    private static int contarPorEstado(String estado) {
        int cantidad = 0;
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).estado.equals(estado)) cantidad++;
        }
        return cantidad;
    }

    private static int contarPorPrioridad(String prioridad) {
        int cantidad = 0;
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).prioridad.equals(prioridad)) cantidad++;
        }
        return cantidad;
    }

    private static int contarSinResponsable() {
        int cantidad = 0;
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).responsable == null) cantidad++;
        }
        return cantidad;
    }

    private static double calcularPlazoPromedio() {
        if (solicitudes.size() == 0) return 0.0;
        int suma = 0;
        for (int i = 0; i < solicitudes.size(); i++) {
            suma = suma + calcularPlazo(solicitudes.get(i));
        }
        double promedio = (double) suma / solicitudes.size();
        return Math.round(promedio * 10) / 10.0;
    }
}
