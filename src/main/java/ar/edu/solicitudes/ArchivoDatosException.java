package ar.edu.solicitudes;

// Error al leer o escribir el archivo de datos. Antes estos fallos se silenciaban (catch vacio).
public class ArchivoDatosException extends RuntimeException {

    public ArchivoDatosException(String mensaje) {
        super(mensaje);
    }

    public ArchivoDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
