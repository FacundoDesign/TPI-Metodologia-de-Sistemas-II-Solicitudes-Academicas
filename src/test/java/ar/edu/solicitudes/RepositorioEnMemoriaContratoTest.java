package ar.edu.solicitudes;

public class RepositorioEnMemoriaContratoTest extends ContratoSolicitudRepository {

    @Override
    SolicitudRepository crear() {
        return new RepositorioEnMemoria();
    }
}
