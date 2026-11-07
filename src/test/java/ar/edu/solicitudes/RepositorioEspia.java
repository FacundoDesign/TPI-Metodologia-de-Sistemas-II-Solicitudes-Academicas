package ar.edu.solicitudes;

import java.util.ArrayList;
import java.util.List;

/**
 * MOCK escrito a mano (sin librerias): ademas de devolver datos, REGISTRA el protocolo de interaccion
 * (que metodos se invocaron, cuantas veces y con que contenido) para que la prueba lo verifique.
 * Diferencia con RepositorioEnMemoria (un fake con estado propio) y con un stub (respuestas enlatadas).
 */
final class RepositorioEspia implements SolicitudRepository {

    private final List<Solicitud> inicial;
    int llamadasACargar = 0;
    private final List<List<String>> fotosDeCadaGuardado = new ArrayList<>();

    RepositorioEspia(List<Solicitud> inicial) {
        this.inicial = new ArrayList<>(inicial);
    }

    @Override
    public List<Solicitud> cargarTodas() {
        llamadasACargar++;
        return new ArrayList<>(inicial);
    }

    @Override
    public void guardarTodas(List<Solicitud> solicitudes) {
        List<String> foto = new ArrayList<>();
        for (Solicitud s : solicitudes) {
            foto.add(s.id() + ":" + s.estado() + ":" + (s.tieneResponsable() ? s.responsable() : "-"));
        }
        fotosDeCadaGuardado.add(foto);
    }

    int cantidadDeGuardados() {
        return fotosDeCadaGuardado.size();
    }

    List<String> contenidoDelGuardado(int numero) {
        return fotosDeCadaGuardado.get(numero - 1);
    }
}
