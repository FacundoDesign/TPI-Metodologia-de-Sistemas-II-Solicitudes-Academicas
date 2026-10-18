package ar.edu.solicitudes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Casos de uso del dominio (SRP): registrar, cambiar estado, asignar responsable y buscar.
// No sabe de consola ni de archivos: recibe el repositorio por constructor (inyeccion de dependencias, DIP),
// por eso se puede probar con un repositorio en memoria y sin estado global estatico.
public class SolicitudService {

    private final SolicitudRepository repositorio;
    private final List<Solicitud> solicitudes;
    private int proximoId;

    public SolicitudService(SolicitudRepository repositorio) {
        this.repositorio = repositorio;
        this.solicitudes = new ArrayList<>(repositorio.cargarTodas());
        this.proximoId = calcularProximoId();
    }

    public Solicitud registrar(NuevaSolicitud datos) {
        Solicitud nueva = Solicitud.nueva(proximoId, datos);
        proximoId++;
        solicitudes.add(nueva);
        guardar();
        return nueva;
    }

    public void cambiarEstado(int id, Estado nuevoEstado) {
        obtener(id).pasarA(nuevoEstado);
        guardar();
    }

    public void asignarResponsable(int id, Responsable responsable) {
        obtener(id).asignarA(responsable);
        guardar();
    }

    // lanza SolicitudException si no existe
    public Solicitud obtener(int id) {
        for (Solicitud s : solicitudes) {
            if (s.id() == id) return s;
        }
        throw new SolicitudException("no existe la solicitud " + id);
    }

    public List<Solicitud> buscar(FiltroSolicitudes filtro) {
        List<Solicitud> resultado = new ArrayList<>();
        for (Solicitud s : solicitudes) {
            if (filtro.acepta(s)) resultado.add(s);
        }
        return resultado;
    }

    public List<Solicitud> todas() {
        return Collections.unmodifiableList(solicitudes);
    }

    private int calcularProximoId() {
        int maximo = 0;
        for (Solicitud s : solicitudes) {
            maximo = Math.max(maximo, s.id());
        }
        return maximo + 1;
    }

    private void guardar() {
        repositorio.guardarTodas(solicitudes);
    }
}
