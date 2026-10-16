package ar.edu.solicitudes;

public enum Responsable {
    BEDELIA, ALUMNADO, SECRETARIA;

    public static Responsable desde(String texto) {
        return Enums.buscar(Responsable.class, texto);
    }
}
