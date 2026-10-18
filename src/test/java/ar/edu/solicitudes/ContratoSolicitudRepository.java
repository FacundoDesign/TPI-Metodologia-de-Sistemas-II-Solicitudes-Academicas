package ar.edu.solicitudes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * Prueba de contrato (Unidad 3): todas las implementaciones de SolicitudRepository deben comportarse igual.
 * Es la evidencia de LSP: cualquiera puede sustituir a otra sin que el servicio lo note.
 * Cada implementacion extiende esta clase y solo aporta crear().
 */
abstract class ContratoSolicitudRepository {

    abstract SolicitudRepository crear() throws Exception;

    @Test
    public void unRepositorioNuevoEstaVacio() throws Exception {
        assertTrue(crear().cargarTodas().isEmpty());
    }

    @Test
    public void loGuardadoSeRecuperaIgual() throws Exception {
        SolicitudRepository repo = crear();
        Solicitud abierta = DatosDePrueba.solicitud(1, TipoSolicitud.EQUIVALENCIA, Prioridad.MEDIA, Estado.EN_PROCESO, Responsable.BEDELIA);
        Solicitud sinResponsable = Solicitud.reconstruir(2, new Estudiante("23456", "Luis Gomez", "luis@mail.com"),
                TipoSolicitud.CERTIFICADO, "otra descripcion", Prioridad.BAJA, Estado.PENDIENTE, null, true);

        repo.guardarTodas(List.of(abierta, sinResponsable));
        List<Solicitud> leidas = repo.cargarTodas();

        assertEquals(2, leidas.size());
        assertIguales(abierta, leidas.get(0));
        assertIguales(sinResponsable, leidas.get(1));
    }

    @Test
    public void guardarReemplazaElContenidoAnterior() throws Exception {
        SolicitudRepository repo = crear();
        repo.guardarTodas(List.of(DatosDePrueba.solicitud(1, TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.PENDIENTE, null),
                DatosDePrueba.solicitud(2, TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.PENDIENTE, null)));

        repo.guardarTodas(List.of(DatosDePrueba.solicitud(3, TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.PENDIENTE, null)));

        assertEquals(1, repo.cargarTodas().size());
        assertEquals(3, repo.cargarTodas().get(0).id());
    }

    static void assertIguales(Solicitud esperada, Solicitud obtenida) {
        assertEquals(esperada.id(), obtenida.id());
        assertEquals(esperada.estudiante(), obtenida.estudiante());
        assertEquals(esperada.tipo(), obtenida.tipo());
        assertEquals(esperada.descripcion(), obtenida.descripcion());
        assertEquals(esperada.prioridad(), obtenida.prioridad());
        assertEquals(esperada.estado(), obtenida.estado());
        assertEquals(esperada.responsable(), obtenida.responsable());
        assertEquals(esperada.urgente(), obtenida.urgente());
    }
}
