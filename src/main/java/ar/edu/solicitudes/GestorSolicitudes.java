package ar.edu.solicitudes;

import java.io.*;
import java.util.*;

// Gestor de solicitudes: carga, guarda, valida, calcula y reporta.
// Refactoring 1 (Extract Method + Rename): metodos cortos, sin duplicacion ni codigo muerto.
// Refactoring 2 (Replace Type Code with Enum): estado, tipo, prioridad y responsable pasan de String a enum.
// Refactoring 3 (Move Method): las reglas de negocio se mudaron a Estado, Prioridad, TipoSolicitud y Solicitud.
// Refactoring 4 (Introduce Parameter Object): Estudiante, NuevaSolicitud y FiltroSolicitudes.
// Refactoring 5 (Replace Error Code with Exception): los codigos -1..-4 pasan a SolicitudException y los
// catch vacios a ArchivoDatosException. La estructura estatica todavia no cambia.
public class GestorSolicitudes {

    public static List<Solicitud> solicitudes = new ArrayList<Solicitud>();
    public static int proximoId = 1;
    static String ARCHIVO_DATOS = "solicitudes.csv";

    // ------------------------------------------------------------------ persistencia

    public static void cargar() {
        File archivo = new File(ARCHIVO_DATOS);
        if (!archivo.exists()) return;
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int numeroDeLinea = 0;
            while ((linea = lector.readLine()) != null) {
                numeroDeLinea++;
                Solicitud s = parsearLinea(linea, numeroDeLinea);
                solicitudes.add(s);
                if (s.id >= proximoId) proximoId = s.id + 1;
            }
        } catch (IOException e) {
            throw new ArchivoDatosException("no se pudo leer " + ARCHIVO_DATOS, e);
        }
    }

    public static void guardar() {
        try (PrintWriter escritor = new PrintWriter(new FileWriter(ARCHIVO_DATOS))) {
            for (int i = 0; i < solicitudes.size(); i++) {
                escritor.println(serializar(solicitudes.get(i)));
            }
            if (escritor.checkError()) throw new IOException("error de escritura");
        } catch (IOException e) {
            throw new ArchivoDatosException("no se pudo guardar " + ARCHIVO_DATOS, e);
        }
    }

    private static Solicitud parsearLinea(String linea, int numeroDeLinea) {
        String[] campos = linea.split(";", -1);
        if (campos.length != 10) {
            throw new ArchivoDatosException(ARCHIVO_DATOS + ", linea " + numeroDeLinea + ": se esperaban 10 campos y hay " + campos.length);
        }
        try {
            Solicitud s = new Solicitud();
            s.id = Integer.parseInt(campos[0]);
            s.estudiante = new Estudiante(campos[1], campos[2], campos[3]);
            s.tipo = TipoSolicitud.desde(campos[4]);
            s.descripcion = campos[5];
            s.prioridad = Prioridad.desde(campos[6]);
            s.estado = Estado.desde(campos[7]);
            s.responsable = Responsable.desde(campos[8]);
            s.urgente = campos[9].equals("1");
            if (s.tipo == null || s.prioridad == null || s.estado == null) {
                throw new ArchivoDatosException(ARCHIVO_DATOS + ", linea " + numeroDeLinea + ": tipo, prioridad o estado desconocido");
            }
            return s;
        } catch (NumberFormatException | SolicitudException e) {
            throw new ArchivoDatosException(ARCHIVO_DATOS + ", linea " + numeroDeLinea + ": " + e.getMessage(), e);
        }
    }

    private static String serializar(Solicitud s) {
        String responsable = s.responsable == null ? "-" : s.responsable.name();
        String urgente = s.urgente ? "1" : "0";
        return s.id + ";" + s.estudiante.legajo() + ";" + s.estudiante.nombre() + ";" + s.estudiante.email() + ";" + s.tipo + ";"
                + s.descripcion + ";" + s.prioridad + ";" + s.estado + ";" + responsable + ";" + urgente;
    }

    // ------------------------------------------------------------------ casos de uso

    public static Solicitud registrar(NuevaSolicitud datos) {
        TipoSolicitud tipo = TipoSolicitud.desde(datos.tipo());
        if (tipo == null) throw new SolicitudException("tipo de solicitud desconocido");

        Solicitud nueva = new Solicitud();
        nueva.id = proximoId;
        proximoId++;
        nueva.estudiante = datos.estudiante();
        nueva.tipo = tipo;
        nueva.descripcion = datos.descripcion() == null ? "" : datos.descripcion();
        nueva.prioridad = tipo.prioridadInicial(datos.urgente());
        nueva.estado = Estado.PENDIENTE;
        nueva.responsable = null;
        nueva.urgente = datos.urgente();
        solicitudes.add(nueva);
        guardar();
        return nueva;
    }

    public static void cambiarEstado(int id, String textoEstado) {
        Solicitud s = obtenerPorId(id);
        Estado nuevoEstado = Estado.desde(textoEstado);
        if (nuevoEstado == null) throw new SolicitudException("estado desconocido");
        if (!s.estado.puedePasarA(nuevoEstado)) {
            throw new SolicitudException("transicion invalida " + s.estado + " -> " + nuevoEstado);
        }
        if (nuevoEstado == Estado.EN_PROCESO && !s.tieneResponsable()) {
            throw new SolicitudException("debe asignar responsable antes de pasar a EN_PROCESO");
        }
        s.estado = nuevoEstado;
        guardar();
    }

    public static void asignarResponsable(int id, String textoResponsable) {
        Solicitud s = obtenerPorId(id);
        Responsable responsable = Responsable.desde(textoResponsable);
        if (responsable == null) throw new SolicitudException("responsable desconocido");
        if (s.estaCerrada()) throw new SolicitudException("la solicitud esta cerrada");
        s.responsable = responsable;
        guardar();
    }

    private static Solicitud obtenerPorId(int id) {
        Solicitud s = buscarPorId(id);
        if (s == null) throw new SolicitudException("no existe la solicitud " + id);
        return s;
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

    public static List<Solicitud> buscar(FiltroSolicitudes filtro) {
        List<Solicitud> resultado = new ArrayList<Solicitud>();
        for (int i = 0; i < solicitudes.size(); i++) {
            Solicitud s = solicitudes.get(i);
            if (filtro.acepta(s)) resultado.add(s);
        }
        return resultado;
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
