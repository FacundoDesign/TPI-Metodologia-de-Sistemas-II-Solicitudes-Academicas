package ar.edu.solicitudes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Cumple el contrato comun (hereda sus pruebas) y agrega lo propio del formato CSV. */
public class CsvSolicitudRepositoryTest extends ContratoSolicitudRepository {

    private static final char BARRA = (char) 92; // barra invertida

    @Rule
    public TemporaryFolder carpeta = new TemporaryFolder();

    @Override
    SolicitudRepository crear() throws Exception {
        return new CsvSolicitudRepository(carpeta.getRoot().toPath().resolve("solicitudes.csv"));
    }

    private Path archivo() {
        return carpeta.getRoot().toPath().resolve("solicitudes.csv");
    }

    @Test
    public void elFormatoDeLosDatosNormalesNoCambioRespectoDeLaVersionInicial() throws Exception {
        SolicitudRepository repo = crear();

        repo.guardarTodas(List.of(DatosDePrueba.solicitud(1, TipoSolicitud.EQUIVALENCIA, Prioridad.MEDIA, new Seguimiento(Estado.EN_PROCESO, Responsable.BEDELIA))));

        assertEquals("1;12345;Ana Perez;ana@mail.com;EQUIVALENCIA;descripcion de prueba;MEDIA;EN_PROCESO;BEDELIA;0"
                + System.lineSeparator(), Files.readString(archivo(), StandardCharsets.UTF_8));
    }

    @Test
    public void unPuntoYComaEnUnTextoNoCorrompeElRegistro() throws Exception {
        SolicitudRepository repo = crear();
        Estudiante estudiante = new Estudiante("12345", "Perez; Ana", "ana@mail.com");
        Solicitud conSeparador = Solicitud.reconstruir(1,
                new NuevaSolicitud(estudiante, TipoSolicitud.CERTIFICADO, "Nota; urgente para beca", false),
                Prioridad.BAJA, new Seguimiento(Estado.PENDIENTE, null));

        repo.guardarTodas(List.of(conSeparador));
        List<Solicitud> leidas = repo.cargarTodas();

        assertEquals(1, leidas.size());
        assertIguales(conSeparador, leidas.get(0));
    }

    @Test
    public void tambienSePreservaLaBarraInvertida() throws Exception {
        SolicitudRepository repo = crear();
        String descripcion = "ruta C:" + BARRA + "tmp" + BARRA + ";fin" + BARRA;
        Solicitud conBarra = Solicitud.reconstruir(1,
                new NuevaSolicitud(DatosDePrueba.ana(), TipoSolicitud.CERTIFICADO, descripcion, false),
                Prioridad.BAJA, new Seguimiento(Estado.PENDIENTE, null));

        repo.guardarTodas(List.of(conBarra));

        assertIguales(conBarra, repo.cargarTodas().get(0));
    }

    @Test
    public void unaLineaConCantidadDeCamposIncorrectaSeInformaConSuNumero() throws Exception {
        Files.writeString(archivo(), "1;12345;Ana\n", StandardCharsets.UTF_8);

        ArchivoDatosException e = assertThrows(ArchivoDatosException.class, () -> crear().cargarTodas());

        assertTrue(e.getMessage(), e.getMessage().contains("linea 1"));
        assertTrue(e.getMessage(), e.getMessage().contains("se esperaban 10 campos y hay 3"));
    }

    @Test
    public void unEstadoDesconocidoSeInforma() throws Exception {
        Files.writeString(archivo(), "1;12345;Ana Perez;ana@mail.com;CERTIFICADO;d;BAJA;COLGADA;-;0\n", StandardCharsets.UTF_8);

        ArchivoDatosException e = assertThrows(ArchivoDatosException.class, () -> crear().cargarTodas());

        assertTrue(e.getMessage(), e.getMessage().contains("tipo, prioridad o estado desconocido"));
    }

    @Test
    public void unIdNoNumericoSeInformaConSuLinea() throws Exception {
        Files.writeString(archivo(), "x;12345;Ana Perez;ana@mail.com;CERTIFICADO;d;BAJA;PENDIENTE;-;0\n", StandardCharsets.UTF_8);

        ArchivoDatosException e = assertThrows(ArchivoDatosException.class, () -> crear().cargarTodas());

        assertTrue(e.getMessage(), e.getMessage().contains("linea 1"));
    }

    @Test
    public void unDatoDeEstudianteInvalidoEnElArchivoSeInforma() throws Exception {
        Files.writeString(archivo(), "1;123;Ana Perez;ana@mail.com;CERTIFICADO;d;BAJA;PENDIENTE;-;0\n", StandardCharsets.UTF_8);

        ArchivoDatosException e = assertThrows(ArchivoDatosException.class, () -> crear().cargarTodas());

        assertTrue(e.getMessage(), e.getMessage().contains("legajo invalido"));
    }

    @Test
    public void guardarEnUnaCarpetaInexistenteFallaConUnErrorClaro() {
        SolicitudRepository repo = new CsvSolicitudRepository(carpeta.getRoot().toPath().resolve("no-existe").resolve("x.csv"));

        ArchivoDatosException e = assertThrows(ArchivoDatosException.class, () -> repo.guardarTodas(List.of()));

        assertTrue(e.getMessage(), e.getMessage().startsWith("no se pudo guardar"));
    }
}
