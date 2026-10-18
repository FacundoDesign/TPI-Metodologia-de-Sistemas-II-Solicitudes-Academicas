package ar.edu.solicitudes;

import java.util.ArrayList;
import java.util.List;

/**
 * Doble de prueba (stub + espia) del repositorio: guarda en memoria, cuenta cuantas veces se guardo y puede
 * simular un fallo de disco. Existe gracias a la inversion de dependencias (DIP): el servicio solo conoce la interfaz.
 */
final class RepositorioEnMemoria implements SolicitudRepository {

    private List<Solicitud> datos = new ArrayList<>();
    int cantidadDeGuardados = 0;
    boolean fallarAlGuardar = false;

    RepositorioEnMemoria() {
    }

    RepositorioEnMemoria(List<Solicitud> inicial) {
        this.datos = new ArrayList<>(inicial);
    }

    @Override
    public List<Solicitud> cargarTodas() {
        return new ArrayList<>(datos);
    }

    @Override
    public void guardarTodas(List<Solicitud> solicitudes) {
        if (fallarAlGuardar) throw new ArchivoDatosException("disco lleno");
        datos = new ArrayList<>(solicitudes);
        cantidadDeGuardados++;
    }
}
