package ar.edu.solicitudes;

import java.util.List;

// Puerto de persistencia (DIP): el servicio depende de esta abstraccion, no de archivos ni de CSV.
// Cambiar a base de datos implica escribir otra implementacion, sin tocar el servicio (OCP).
public interface SolicitudRepository {

    // lanza ArchivoDatosException si los datos no se pueden leer o estan danados
    List<Solicitud> cargarTodas();

    // lanza ArchivoDatosException si no se pueden guardar
    void guardarTodas(List<Solicitud> solicitudes);
}
