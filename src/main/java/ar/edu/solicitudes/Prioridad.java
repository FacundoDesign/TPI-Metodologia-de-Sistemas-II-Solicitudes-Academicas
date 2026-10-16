package ar.edu.solicitudes;

public enum Prioridad {
    ALTA, MEDIA, BAJA;

    public static Prioridad desde(String texto) {
        return Enums.buscar(Prioridad.class, texto);
    }
}
