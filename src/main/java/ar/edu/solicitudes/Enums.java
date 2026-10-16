package ar.edu.solicitudes;

// Utilidad comun a los enums del dominio: evita repetir el mismo bucle de busqueda por nombre en cada uno (DRY).
final class Enums {

    private Enums() {
    }

    // devuelve la constante cuyo nombre coincide exactamente con el texto, o null si no existe
    static <E extends Enum<E>> E buscar(Class<E> tipo, String texto) {
        for (E constante : tipo.getEnumConstants()) {
            if (constante.name().equals(texto)) return constante;
        }
        return null;
    }
}
