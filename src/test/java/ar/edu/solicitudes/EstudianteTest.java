package ar.edu.solicitudes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class EstudianteTest {

    private void assertInvalido(String mensajeEsperado, String legajo, String nombre, String email) {
        try {
            new Estudiante(legajo, nombre, email);
            fail("Se esperaba SolicitudException: " + mensajeEsperado);
        } catch (SolicitudException e) {
            assertEquals(mensajeEsperado, e.getMessage());
        }
    }

    @Test
    public void datosValidosCreanElEstudiante() {
        Estudiante e = new Estudiante("12345", "Ana Perez", "ana@mail.com");
        assertEquals("12345", e.legajo());
        assertEquals("Ana Perez", e.nombre());
        assertEquals("ana@mail.com", e.email());
    }

    @Test
    public void legajoDebeTenerExactamenteCincoDigitos() {
        String msg = "legajo invalido (se esperan 5 digitos)";
        assertInvalido(msg, "1234", "Ana", "a@b.c");
        assertInvalido(msg, "123456", "Ana", "a@b.c");
        assertInvalido(msg, "12a45", "Ana", "a@b.c");
        assertInvalido(msg, "", "Ana", "a@b.c");
        assertInvalido(msg, null, "Ana", "a@b.c");
    }

    @Test
    public void nombreNoPuedeEstarVacioNiEnBlanco() {
        String msg = "el nombre no puede estar vacio";
        assertInvalido(msg, "12345", "", "a@b.c");
        assertInvalido(msg, "12345", "   ", "a@b.c");
        assertInvalido(msg, "12345", null, "a@b.c");
    }

    @Test
    public void emailDebeTenerArrobaNoInicialYUnPuntoPosterior() {
        String msg = "email invalido";
        assertInvalido(msg, "12345", "Ana", "ana");
        assertInvalido(msg, "12345", "Ana", "ana@mail");
        assertInvalido(msg, "12345", "Ana", "@mail.com");
        assertInvalido(msg, "12345", "Ana", "a.b@c");
        assertInvalido(msg, "12345", "Ana", null);
        new Estudiante("12345", "Ana", "a@b.c");
    }

    @Test
    public void seValidaEnOrdenLegajoNombreEmail() {
        assertInvalido("legajo invalido (se esperan 5 digitos)", "x", "", "malo");
        assertInvalido("el nombre no puede estar vacio", "12345", "", "malo");
    }

    @Test
    public void unEmailComoArrobaPuntoSeAceptaPorComportamientoHeredado() {
        // Caracterizacion: la version inicial acepta "a@.c". Se conserva a proposito (refactoring != cambio de reglas)
        // y queda registrado como mejora futura en docs/05-conclusiones.md.
        new Estudiante("12345", "Ana", "a@.c");
    }
}
