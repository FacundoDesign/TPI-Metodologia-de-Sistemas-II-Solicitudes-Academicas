package ar.edu.solicitudes;

import java.io.*;
import java.util.*;

// Gestor de solicitudes: carga, guarda, valida, calcula y reporta.
// Refactoring 1 (Extract Method + Rename): metodos cortos, sin duplicacion ni codigo muerto.
// Refactoring 2 (Replace Type Code with Enum): estado, tipo, prioridad y responsable pasan de String a enum.
// Refactoring 3 (Move Method): las reglas de negocio se mudaron a Estado, Prioridad, TipoSolicitud y Solicitud.
// Los codigos de error y la estructura general todavia no cambian.
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
        s.tipo = TipoSolicitud.desde(campos[4]);
        s.descripcion = campos[5];
        s.prioridad = Prioridad.desde(campos[6]);
        s.estado = Estado.desde(campos[7]);
        s.responsable = Responsable.desde(campos[8]);
        s.urgente = campos[9].equals("1");
        return s;
    }

    private static String serializar(Solicitud s) {
        String responsable = s.responsable == null ? "-" : s.responsable.name();
        String urgente = s.urgente ? "1" : "0";
        return s.id + ";" + s.legajo + ";" + s.nombre + ";" + s.email + ";" + s.tipo + ";"
                + s.descripcion + ";" + s.prioridad + ";" + s.estado + ";" + responsable + ";" + urgente;
    }

    // ------------------------------------------------------------------ casos de uso

    // devuelve el id de la nueva solicitud, o un codigo negativo si hay error
    // -1 legajo, -2 nombre, -3 email, -4 tipo
    public static int registrar(String legajo, String nombre, String email, String textoTipo, String descripcion, boolean urgente) {
        if (!esLegajoValido(legajo)) return -1;
        if (!esNombreValido(nombre)) return -2;
        if (!esEmailValido(email)) return -3;
        TipoSolicitud tipo = TipoSolicitud.desde(textoTipo);
        if (tipo == null) return -4;

        Solicitud nueva = new Solicitud();
        nueva.id = proximoId;
        proximoId++;
        nueva.legajo = legajo;
        nueva.nombre = nombre;
        nueva.email = email;
        nueva.tipo = tipo;
        nueva.descripcion = descripcion == null ? "" : descripcion;
        nueva.prioridad = tipo.prioridadInicial(urgente);
        nueva.estado = Estado.PENDIENTE;
        nueva.responsable = null;
        nueva.urgente = urgente;
        solicitudes.add(nueva);
        guardar();
        return nueva.id;
    }

    // 0 ok, -1 no existe, -2 estado desconocido, -3 transicion invalida, -4 falta responsable
    public static int cambiarEstado(int id, String textoEstado) {
        Solicitud s = buscarPorId(id);
        if (s == null) return -1;
        Estado nuevoEstado = Estado.desde(textoEstado);
        if (nuevoEstado == null) return -2;
        if (!s.estado.puedePasarA(nuevoEstado)) return -3;
        if (nuevoEstado == Estado.EN_PROCESO && !s.tieneResponsable()) return -4;
        s.estado = nuevoEstado;
        guardar();
        return 0;
    }

    // 0 ok, -1 no existe, -2 responsable desconocido, -3 solicitud cerrada
    public static int asignarResponsable(int id, String textoResponsable) {
        Solicitud s = buscarPorId(id);
        if (s == null) return -1;
        Responsable responsable = Responsable.desde(textoResponsable);
        if (responsable == null) return -2;
        if (s.estaCerrada()) return -3;
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
        String responsableActual = s.responsable == null ? "" : s.responsable.name();
        return coincide(estado, s.estado.name())
                && coincide(tipo, s.tipo.name())
                && coincide(prioridad, s.prioridad.name())
                && coincide(responsable, responsableActual)
                && coincide(legajo, s.legajo);
    }

    // un filtro vacio significa "todos"
    private static boolean coincide(String filtro, String valor) {
        return filtro.equals("") || valor.equals(filtro);
    }

    // ------------------------------------------------------------------ validaciones de entrada

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

    // ------------------------------------------------------------------ reporte

    public static void imprimirReporte() {
        int total = solicitudes.size();
        System.out.println("--- REPORTE ---");
        System.out.println("Total de solicitudes: " + total);
        System.out.println("Por estado:");
        System.out.println("  PENDIENTE: " + contarPorEstado(Estado.PENDIENTE));
        System.out.println("  EN_PROCESO: " + contarPorEstado(Estado.EN_PROCESO));
        System.out.println("  RESUELTA: " + contarPorEstado(Estado.RESUELTA));
        System.out.println("  RECHAZADA: " + contarPorEstado(Estado.RECHAZADA));
        System.out.println("Por prioridad:");
        System.out.println("  ALTA: " + contarPorPrioridad(Prioridad.ALTA));
        System.out.println("  MEDIA: " + contarPorPrioridad(Prioridad.MEDIA));
        System.out.println("  BAJA: " + contarPorPrioridad(Prioridad.BAJA));
        System.out.println("Sin responsable asignado: " + contarSinResponsable());
        System.out.println("Plazo promedio de respuesta: " + calcularPlazoPromedio() + " dias");
    }

    private static int contarPorEstado(Estado estado) {
        int cantidad = 0;
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).estado == estado) cantidad++;
        }
        return cantidad;
    }

    private static int contarPorPrioridad(Prioridad prioridad) {
        int cantidad = 0;
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).prioridad == prioridad) cantidad++;
        }
        return cantidad;
    }

    private static int contarSinResponsable() {
        int cantidad = 0;
        for (int i = 0; i < solicitudes.size(); i++) {
            if (!solicitudes.get(i).tieneResponsable()) cantidad++;
        }
        return cantidad;
    }

    private static double calcularPlazoPromedio() {
        if (solicitudes.size() == 0) return 0.0;
        int suma = 0;
        for (int i = 0; i < solicitudes.size(); i++) {
            suma = suma + solicitudes.get(i).calcularPlazo();
        }
        double promedio = (double) suma / solicitudes.size();
        return Math.round(promedio * 10) / 10.0;
    }
}
