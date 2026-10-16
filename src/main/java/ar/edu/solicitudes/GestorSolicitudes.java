package ar.edu.solicitudes;

import java.io.*;
import java.util.*;

// Gestor de solicitudes: carga, guarda, valida, calcula y reporta.
public class GestorSolicitudes {

    public static List<Solicitud> db = new ArrayList<Solicitud>();
    public static int sig = 1;
    static String ARCH = "solicitudes.csv";

    public static void cargar() {
        try {
            File f = new File(ARCH);
            if (!f.exists()) return;
            BufferedReader br = new BufferedReader(new FileReader(f));
            String l;
            while ((l = br.readLine()) != null) {
                String[] p = l.split(";");
                Solicitud s = new Solicitud();
                s.id = Integer.parseInt(p[0]);
                s.legajo = p[1];
                s.nombre = p[2];
                s.email = p[3];
                s.tipo = p[4];
                s.descripcion = p[5];
                s.prioridad = p[6];
                s.estado = p[7];
                if (p[8].equals("-")) s.responsable = null; else s.responsable = p[8];
                if (p[9].equals("1")) s.urgente = true; else s.urgente = false;
                db.add(s);
                if (s.id >= sig) sig = s.id + 1;
            }
            br.close();
        } catch (Exception e) {
        }
    }

    public static void guardar() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(ARCH));
            for (int i = 0; i < db.size(); i++) {
                Solicitud s = db.get(i);
                String r = s.responsable == null ? "-" : s.responsable;
                String u = s.urgente ? "1" : "0";
                pw.println(s.id + ";" + s.legajo + ";" + s.nombre + ";" + s.email + ";" + s.tipo + ";"
                        + s.descripcion + ";" + s.prioridad + ";" + s.estado + ";" + r + ";" + u);
            }
            pw.close();
        } catch (Exception e) {
        }
    }

    // devuelve el id de la nueva solicitud, o un codigo negativo si hay error
    // -1 legajo, -2 nombre, -3 email, -4 tipo
    public static int registrar(String legajo, String nombre, String email, String tipo, String descripcion, boolean urgente) {
        if (legajo == null || legajo.length() != 5) return -1;
        for (int i = 0; i < legajo.length(); i++) {
            if (!Character.isDigit(legajo.charAt(i))) return -1;
        }
        if (nombre == null || nombre.trim().equals("")) return -2;
        if (email == null || email.indexOf("@") < 1 || email.lastIndexOf(".") < email.indexOf("@")) return -3;
        if (!(tipo.equals("CERTIFICADO") || tipo.equals("EQUIVALENCIA") || tipo.equals("REVISION_NOTA") || tipo.equals("BAJA_MATERIA"))) return -4;

        String p;
        if (tipo.equals("REVISION_NOTA")) p = "ALTA";
        else if (tipo.equals("EQUIVALENCIA")) p = "MEDIA";
        else if (tipo.equals("BAJA_MATERIA")) p = "MEDIA";
        else p = "BAJA";
        if (urgente) {
            if (p.equals("BAJA")) p = "MEDIA";
            else if (p.equals("MEDIA")) p = "ALTA";
        }

        Solicitud s = new Solicitud();
        s.id = sig;
        sig++;
        s.legajo = legajo;
        s.nombre = nombre;
        s.email = email;
        s.tipo = tipo;
        s.descripcion = descripcion == null ? "" : descripcion;
        s.prioridad = p;
        s.estado = "PENDIENTE";
        s.responsable = null;
        s.urgente = urgente;
        db.add(s);
        guardar();
        return s.id;
    }

    // 0 ok, -1 no existe, -2 estado desconocido, -3 transicion invalida, -4 falta responsable
    public static int cambiarEstado(int id, String nuevo) {
        Solicitud s = null;
        for (int i = 0; i < db.size(); i++) {
            if (db.get(i).id == id) {
                s = db.get(i);
            }
        }
        if (s == null) return -1;
        if (!(nuevo.equals("PENDIENTE") || nuevo.equals("EN_PROCESO") || nuevo.equals("RESUELTA") || nuevo.equals("RECHAZADA"))) return -2;
        boolean ok = false;
        if (s.estado.equals("PENDIENTE")) {
            if (nuevo.equals("EN_PROCESO") || nuevo.equals("RECHAZADA")) ok = true;
        } else if (s.estado.equals("EN_PROCESO")) {
            if (nuevo.equals("RESUELTA") || nuevo.equals("RECHAZADA")) ok = true;
        }
        if (!ok) return -3;
        if (nuevo.equals("EN_PROCESO") && s.responsable == null) return -4;
        s.estado = nuevo;
        guardar();
        return 0;
    }

    // 0 ok, -1 no existe, -2 responsable desconocido, -3 solicitud cerrada
    public static int asignarResponsable(int id, String resp) {
        Solicitud s = null;
        for (int i = 0; i < db.size(); i++) {
            if (db.get(i).id == id) {
                s = db.get(i);
            }
        }
        if (s == null) return -1;
        if (!(resp.equals("BEDELIA") || resp.equals("ALUMNADO") || resp.equals("SECRETARIA"))) return -2;
        if (s.estado.equals("RESUELTA") || s.estado.equals("RECHAZADA")) return -3;
        s.responsable = resp;
        guardar();
        return 0;
    }

    public static Solicitud buscarPorId(int id) {
        Solicitud s = null;
        for (int i = 0; i < db.size(); i++) {
            if (db.get(i).id == id) {
                s = db.get(i);
            }
        }
        return s;
    }

    // plazo de respuesta en dias habiles
    public static int calcularPlazo(Solicitud s) {
        int d;
        if (s.prioridad.equals("ALTA")) d = 3;
        else if (s.prioridad.equals("MEDIA")) d = 5;
        else d = 10;
        if (s.tipo.equals("EQUIVALENCIA")) d = d + 5;
        return d;
    }

    public static List<Solicitud> buscar(String estado, String tipo, String prioridad, String responsable, String legajo) {
        List<Solicitud> r = new ArrayList<Solicitud>();
        for (int i = 0; i < db.size(); i++) {
            Solicitud s = db.get(i);
            boolean ok = true;
            if (!estado.equals("") && !s.estado.equals(estado)) ok = false;
            if (!tipo.equals("") && !s.tipo.equals(tipo)) ok = false;
            if (!prioridad.equals("") && !s.prioridad.equals(prioridad)) ok = false;
            String rr = s.responsable == null ? "" : s.responsable;
            if (!responsable.equals("") && !rr.equals(responsable)) ok = false;
            if (!legajo.equals("") && !s.legajo.equals(legajo)) ok = false;
            if (ok) r.add(s);
        }
        return r;
    }

    public static void imprimirReporte() {
        int total = db.size();
        int pend = 0, proc = 0, res = 0, rech = 0;
        int alta = 0, media = 0, baja = 0;
        int sinResp = 0;
        int suma = 0;
        for (int i = 0; i < db.size(); i++) {
            Solicitud s = db.get(i);
            if (s.estado.equals("PENDIENTE")) pend++;
            else if (s.estado.equals("EN_PROCESO")) proc++;
            else if (s.estado.equals("RESUELTA")) res++;
            else if (s.estado.equals("RECHAZADA")) rech++;
            if (s.prioridad.equals("ALTA")) alta++;
            else if (s.prioridad.equals("MEDIA")) media++;
            else if (s.prioridad.equals("BAJA")) baja++;
            if (s.responsable == null) sinResp++;
            suma = suma + calcularPlazo(s);
        }
        double prom = 0;
        if (total > 0) prom = (double) suma / total;
        prom = Math.round(prom * 10) / 10.0;
        System.out.println("--- REPORTE ---");
        System.out.println("Total de solicitudes: " + total);
        System.out.println("Por estado:");
        System.out.println("  PENDIENTE: " + pend);
        System.out.println("  EN_PROCESO: " + proc);
        System.out.println("  RESUELTA: " + res);
        System.out.println("  RECHAZADA: " + rech);
        System.out.println("Por prioridad:");
        System.out.println("  ALTA: " + alta);
        System.out.println("  MEDIA: " + media);
        System.out.println("  BAJA: " + baja);
        System.out.println("Sin responsable asignado: " + sinResp);
        System.out.println("Plazo promedio de respuesta: " + prom + " dias");
    }

    // TODO: exportar a txt (no se usa)
    public static void exportarTxt(String ruta) {
        int x = 0;
        // for (Solicitud s : db) { ... }
    }
}
