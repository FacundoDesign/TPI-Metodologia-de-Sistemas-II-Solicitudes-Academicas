package ar.edu.solicitudes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * Cierra la observacion R-4 del code review (consistencia memoria/disco ante un fallo de escritura) y verifica
 * el protocolo de interaccion con el repositorio usando un STUB (fallarAlGuardar) y un MOCK (RepositorioEspia).
 */
public class SolicitudServiceConsistenciaTest {

    // ---------- STUB: el repositorio falla al guardar; el servicio no debe quedar a medio camino ----------

    @Test
    public void siFallaElGuardadoUnaSolicitudNuevaNoQuedaEnMemoria() {
        // Arrange
        RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
        SolicitudService servicio = new SolicitudService(repositorio);
        repositorio.fallarAlGuardar = true;

        // Act
        assertThrows(ArchivoDatosException.class,
                () -> servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false)));

        // Assert
        assertTrue(servicio.todas().isEmpty());
    }

    @Test
    public void trasUnFalloDeGuardadoElProximoIdNoSeSalta() {
        RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
        SolicitudService servicio = new SolicitudService(repositorio);
        repositorio.fallarAlGuardar = true;
        assertThrows(ArchivoDatosException.class,
                () -> servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false)));
        repositorio.fallarAlGuardar = false;

        Solicitud reintento = servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false));

        assertEquals(1, reintento.id());
    }

    @Test
    public void siFallaElGuardadoElCambioDeEstadoSeRevierte() {
        RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
        SolicitudService servicio = new SolicitudService(repositorio);
        Solicitud s = servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false));
        servicio.asignarResponsable(s.id(), Responsable.BEDELIA);
        repositorio.fallarAlGuardar = true;

        assertThrows(ArchivoDatosException.class, () -> servicio.cambiarEstado(s.id(), Estado.EN_PROCESO));

        assertEquals(Estado.PENDIENTE, servicio.obtener(s.id()).estado());
    }

    @Test
    public void siFallaElGuardadoLaAsignacionDeResponsableSeRevierte() {
        RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
        SolicitudService servicio = new SolicitudService(repositorio);
        Solicitud s = servicio.registrar(DatosDePrueba.pedido(TipoSolicitud.CERTIFICADO, false));
        repositorio.fallarAlGuardar = true;

        assertThrows(ArchivoDatosException.class, () -> servicio.asignarResponsable(s.id(), Responsable.ALUMNADO));

        assertFalse(servicio.obtener(s.id()).tieneResponsable());
    }

    // ---------- MOCK: se verifica el protocolo de interaccion, no solo el estado final ----------

    @Test
    public void elServicioCargaElRepositorioUnaSolaVezAlCrearse() {
        RepositorioEspia espia = new RepositorioEspia(List.of());

        new SolicitudService(espia);

        assertEquals(1, espia.llamadasACargar);
        assertEquals(0, espia.cantidadDeGuardados());
    }

    @Test
    public void cambiarEstadoValidoGuardaUnaVezConElEstadoNuevo() {
        Solicitud s = DatosDePrueba.solicitud(1, TipoSolicitud.CERTIFICADO, Prioridad.BAJA,
                new Seguimiento(Estado.PENDIENTE, Responsable.BEDELIA));
        RepositorioEspia espia = new RepositorioEspia(List.of(s));
        SolicitudService servicio = new SolicitudService(espia);

        servicio.cambiarEstado(1, Estado.EN_PROCESO);

        assertEquals(1, espia.cantidadDeGuardados());
        assertEquals(List.of("1:EN_PROCESO:BEDELIA"), espia.contenidoDelGuardado(1));
    }

    @Test
    public void unaTransicionInvalidaNoInvocaAlRepositorio() {
        Solicitud s = DatosDePrueba.solicitud(1, TipoSolicitud.CERTIFICADO, Prioridad.BAJA, Estado.PENDIENTE, null);
        RepositorioEspia espia = new RepositorioEspia(List.of(s));
        SolicitudService servicio = new SolicitudService(espia);

        assertThrows(SolicitudException.class, () -> servicio.cambiarEstado(1, Estado.RESUELTA));

        assertEquals(0, espia.cantidadDeGuardados());
    }
}
