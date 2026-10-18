package ar.edu.solicitudes;

// Value object que se valida a si mismo: no puede existir un Estudiante con datos invalidos.
// El orden de validacion (legajo, nombre, email) es parte del comportamiento externo.
public record Estudiante(String legajo, String nombre, String email) {

    public Estudiante {
        if (!esLegajoValido(legajo)) throw new SolicitudException("legajo invalido (se esperan 5 digitos)");
        if (!esNombreValido(nombre)) throw new SolicitudException("el nombre no puede estar vacio");
        if (!esEmailValido(email)) throw new SolicitudException("email invalido");
    }

    private static boolean esLegajoValido(String legajo) {
        if (legajo == null || legajo.length() != 5) return false;
        for (int i = 0; i < legajo.length(); i++) {
            if (!Character.isDigit(legajo.charAt(i))) return false;
        }
        return true;
    }

    private static boolean esNombreValido(String nombre) {
        return nombre != null && !nombre.trim().isEmpty();
    }

    private static boolean esEmailValido(String email) {
        return email != null && email.indexOf("@") >= 1 && email.lastIndexOf(".") >= email.indexOf("@");
    }
}
