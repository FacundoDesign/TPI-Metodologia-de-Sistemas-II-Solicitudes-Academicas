package ar.edu.solicitudes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/** Pruebas unitarias de los casos de uso, aisladas del disco y de la consola (patron AAA). */
public class SolicitudServiceTest {

    private RepositorioEnMemoria repositorio;
    private SolicitudService servicio;

    @Before
    public void preparar() {
        repositorio = new RepositorioEnMemoria();
        servicio = new SolicitudService(repositorio);
    }

    @Test
    public void registrarAsignaIdsCorrelativosDesdeUno() {
        Solicitud primera = servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false));
        Solicitud segunda = servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.EQUIVALENCIA, false));

        assertEquals(1, primera.id());
        assertEquals(2, segunda.id());
    }

    @Test
    public void unaSolicitudNuevaNaceSinResponsableYConLaPrioridadDelTipo() {
        Solicitud s = servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.REVISION_NOTA, false));

        assertEquals(Estado.PENDIENTE, s.estado());
        assertFalse(s.tieneResponsable());
        assertEquals(Prioridad.ALTA, s.prioridad());
    }

    @Test
    public void unaSolicitudUrgenteSubeUnNivelDePrioridad() {
        Solicitud normal = servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false));
        Solicitud urgente = servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, true));

        assertEquals(Prioridad.BAJA, normal.prioridad());
        assertEquals(Prioridad.MEDIA, urgente.prioridad());
    }

    @Test
    public void registrarGuardaUnaVezEnElRepositorio() {
        servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false));

        assertEquals(1, repositorio.cantidadDeGuardados);
        assertEquals(1, repositorio.cargarTodas().size());
    }

    @Test
    public void losIdsContinuanDespuesDeCargarDatosExistentes() {
        Solicitud existente = DatosDePrueba.solicitud(7, TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.PENDIENTE, null);
        SolicitudService conDatos = new SolicitudService(new RepositorioEnMemoria(List.of(existente)));

        Solicitud nueva = conDatos.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false));

        assertEquals(8, nueva.id());
    }

    @Test
    public void cambiarEstadoValidoActualizaYPersiste() {
        Solicitud s = servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false));
        servicio.asignarResponsable(s.id(), Responsable.BEDELIA);

        servicio.cambiarEstado(s.id(), Estado.EN_PROCESO);

        assertEquals(Estado.EN_PROCESO, servicio.obtener(s.id()).estado());
        assertEquals(3, repositorio.cantidadDeGuardados);
    }

    @Test
    public void unaSolicitudInexistenteNoSePuedeObtenerNiModificar() {
        SolicitudException e = assertThrows(SolicitudException.class, () -> servicio.cambiarEstado(99, Estado.RECHAZADA));

        assertEquals("no existe la solicitud 99", e.getMessage());
        assertEquals(0, repositorio.cantidadDeGuardados);
    }

    @Test
    public void unaTransicionInvalidaSeRechazaYNoPersiste() {
        Solicitud s = servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false));
        int guardadosAntes = repositorio.cantidadDeGuardados;

        SolicitudException e = assertThrows(SolicitudException.class, () -> servicio.cambiarEstado(s.id(), Estado.RESUELTA));

        assertEquals("transicion invalida PENDIENTE -> RESUELTA", e.getMessage());
        assertEquals(Estado.PENDIENTE, s.estado());
        assertEquals(guardadosAntes, repositorio.cantidadDeGuardados);
    }

    @Test
    public void pasarAEnProcesoExigeResponsableAsignado() {
        Solicitud s = servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false));

        SolicitudException e = assertThrows(SolicitudException.class, () -> servicio.cambiarEstado(s.id(), Estado.EN_PROCESO));

        assertEquals("debe asignar responsable antes de pasar a EN_PROCESO", e.getMessage());
    }

    @Test
    public void noSePuedeAsignarResponsableAUnaSolicitudCerrada() {
        Solicitud s = servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false));
        servicio.cambiarEstado(s.id(), Estado.RECHAZADA);

        SolicitudException e = assertThrows(SolicitudException.class,
                () -> servicio.asignarResponsable(s.id(), Responsable.ALUMNADO));

        assertEquals("la solicitud esta cerrada", e.getMessage());
    }

    @Test
    public void buscarAplicaElFiltroYDevuelveSoloLasQueCoinciden() {
        Solicitud certificado = servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false));
        servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.EQUIVALENCIA, false));

        List<Solicitud> resultado = servicio.buscar(new FiltroSolicitudes("", "CERTIFICADO", "", "", ""));

        assertEquals(1, resultado.size());
        assertSame(certificado, resultado.get(0));
    }

    @Test
    public void todasDevuelveUnaVistaQueNoSePuedeModificar() {
        servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false));

        assertThrows(UnsupportedOperationException.class, () -> servicio.todas().clear());
        assertTrue(servicio.todas().size() == 1);
    }

    @Test
    public void unFalloDelRepositorioSePropagaConSuPropioTipoDeError() {
        repositorio.fallarAlGuardar = true;

        ArchivoDatosException e = assertThrows(ArchivoDatosException.class,
                () -> servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false)));

        assertEquals("disco lleno", e.getMessage());
    }
}
