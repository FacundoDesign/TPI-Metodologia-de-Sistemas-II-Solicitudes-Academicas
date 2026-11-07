package ar.edu.solicitudes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Persistencia en un archivo CSV de 10 campos separados por ';'. Es la unica clase que conoce el formato.
// El separador y la barra invertida se escapan con '\' para que un texto con ';' no corrompa el registro
// (defecto latente de la version inicial, ver docs/01-diagnostico.md). El formato de los datos "normales" no cambia.
public class CsvSolicitudRepository implements SolicitudRepository {

    private static final int CAMPOS_POR_LINEA = 10;
    private static final char SEPARADOR = ';';
    private static final char ESCAPE = '\\';

    private final Path archivo;

    public CsvSolicitudRepository(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public List<Solicitud> cargarTodas() {
        List<Solicitud> solicitudes = new ArrayList<>();
        if (!Files.exists(archivo)) return solicitudes;
        try {
            List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            for (int i = 0; i < lineas.size(); i++) {
                solicitudes.add(parsearLinea(lineas.get(i), i + 1));
            }
        } catch (IOException e) {
            throw new ArchivoDatosException("no se pudo leer " + archivo, e);
        }
        return solicitudes;
    }

    @Override
    public void guardarTodas(List<Solicitud> solicitudes) {
        List<String> lineas = new ArrayList<>();
        for (Solicitud s : solicitudes) {
            lineas.add(serializar(s));
        }
        try {
            Files.write(archivo, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ArchivoDatosException("no se pudo guardar " + archivo, e);
        }
    }

    private Solicitud parsearLinea(String linea, int numeroDeLinea) {
        List<String> campos = separar(linea);
        if (campos.size() != CAMPOS_POR_LINEA) {
            throw new ArchivoDatosException(archivo + ", linea " + numeroDeLinea + ": se esperaban "
                    + CAMPOS_POR_LINEA + " campos y hay " + campos.size());
        }
        try {
            TipoSolicitud tipo = TipoSolicitud.desde(campos.get(4));
            Prioridad prioridad = Prioridad.desde(campos.get(6));
            Estado estado = Estado.desde(campos.get(7));
            if (tipo == null || prioridad == null || estado == null) {
                throw new ArchivoDatosException(archivo + ", linea " + numeroDeLinea + ": tipo, prioridad o estado desconocido");
            }
            Estudiante estudiante = new Estudiante(campos.get(1), campos.get(2), campos.get(3));
            NuevaSolicitud datos = new NuevaSolicitud(estudiante, tipo, campos.get(5), campos.get(9).equals("1"));
            return Solicitud.reconstruir(Integer.parseInt(campos.get(0)), datos, prioridad,
                    new Seguimiento(estado, Responsable.desde(campos.get(8))));
        } catch (NumberFormatException | SolicitudException e) {
            throw new ArchivoDatosException(archivo + ", linea " + numeroDeLinea + ": " + e.getMessage(), e);
        }
    }

    private String serializar(Solicitud s) {
        String responsable = s.tieneResponsable() ? s.responsable().name() : "-";
        return String.join(String.valueOf(SEPARADOR),
                String.valueOf(s.id()),
                escapar(s.estudiante().legajo()),
                escapar(s.estudiante().nombre()),
                escapar(s.estudiante().email()),
                s.tipo().name(),
                escapar(s.descripcion()),
                s.prioridad().name(),
                s.estado().name(),
                responsable,
                s.urgente() ? "1" : "0");
    }

    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace(";", "\\;");
    }

    // divide por ';' respetando los separadores escapados y quita el escape de cada campo
    private static List<String> separar(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == ESCAPE && i + 1 < linea.length()) {
                actual.append(linea.charAt(++i));
            } else if (c == SEPARADOR) {
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos;
    }
}
