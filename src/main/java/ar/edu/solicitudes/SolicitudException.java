package ar.edu.solicitudes;

// Error de negocio o de validacion. El mensaje es el que se le muestra a la persona usuaria.
public class SolicitudException extends RuntimeException {

    public SolicitudException(String mensaje) {
        super(mensaje);
    }
}
